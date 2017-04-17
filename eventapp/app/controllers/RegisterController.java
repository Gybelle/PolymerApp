package controllers;

import play.mvc.*;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Database
import javax.inject.Inject;
import play.db.*;

/**
 * Created by Michelle Gybels on 15/04/2017.
 */
public class RegisterController extends Controller{
    private Database database;
    private String latestError;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String NAME_PATTERN = "^[a-z ,.'-]+$/i";
    private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

    @Inject
    public RegisterController(Database db) {
        this.database = db;
    }

    /**
     * Register a new user.
     * @return Ok if success. BadRequest otherwise.
     */
    public Result registerUser() {
        latestError = "";
        Map<String, String[]> data = request().body().asFormUrlEncoded();
        String firstname, lastname, mail, password, tickets;

        //Extract fields
        firstname = extractTextField(data, "firstname");
        lastname = extractTextField(data, "lastname");
        mail = extractTextField(data, "mail");
        password = extractTextField(data, "password");
        tickets = extractTextField(data, "tickets");

        //Validation
        boolean namesOk = validateNames(firstname, lastname);
        boolean mailOk = validateMail(mail);
        boolean passOk = validatePassword(password);
        boolean ticketsOk = validate(tickets);

        //If validation not ok...
        if (!namesOk || !mailOk || !passOk || !ticketsOk){
            return badRequest("Could not submit registration: " + latestError);
        }

        //Write data to database
        String encryptedPassword = encryptMD5(password);
        boolean toDatabaseOk = writeToDatabase(firstname, lastname, mail, encryptedPassword, tickets);

        if (!toDatabaseOk){
            badRequest("Could not submit registration: " + latestError);
        }

        return ok("Successful Registration");
    }

    /**
     * Get the value of a field of the register form.
     * @param data Data structure containing the fieldnames and corresponding values.
     * @param fieldname Field name to extract the value from.
     * @return The extracted value. Null otherwise.
     */
    private String extractTextField(Map<String, String[]> data, String fieldname){
        try {
            String fieldValue = data.get(fieldname)[0];
            return fieldValue;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Basic validation. Check if the field exist.
     * @param field Field to check.
     * @return True if exists, false otherwise.
     */
    private boolean validate(String field){
        return field != null;
    }

    /**
     * Validate firstname and lastname.
     * @param firstname Value from the firstname field.
     * @param lastname Value from the lastname field.
     * @return True if validation was ok. False otherwise.
     */
    private boolean validateNames(String firstname, String lastname){
        boolean firstnameOk = validate(firstname);
        boolean lastnameOk = validate(lastname);

        if(!firstnameOk || !lastnameOk){
            latestError = "Firstname and lastname are required.";
            return false;
        }

        //Other validation operations can be placed here. Ex. check characters. Can be done with regex.

        return true;
    }

    /**
     * Validate mail.
     * @param mail Value from mail field.
     * @return True if validation was ok. False otherwise.
     */
    private boolean validateMail(String mail){
        boolean mailOk = validate(mail);
        Pattern pattern;
        Matcher matcher;

        if(!mailOk){
            latestError = "Mail is required.";
            return false;
        }

        // Check format: [some text]@[domain].[extension]
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(mail);
        mailOk = matcher.matches();

        if(!mailOk){
            latestError = "Bad mail format.";
            return false;
        }
        return true;
    }

    /**
     * Validate password.
     * @param password Value from password field.
     * @return True if validation was ok. False otherwise.
     */
    private boolean validatePassword(String password){
        boolean passwordOk = validate(password);

        if(!passwordOk){
            latestError = "Password is required.";
            return false;
        }

        //Other validation operations can be placed here. Ex. check characters or password length.
        //Can be done with regex.

        return true;
    }

    /**
     * Write the validated values to SQLite databse.
     * @param firstname Value from firstname field.
     * @param lastname Value from lastname field.
     * @param mail Value from mail field.
     * @param password Value from password field, encrypted.
     * @param tickets Value from tickets field.
     * @return True if insertion was a success. False otherwise.
     */
    private boolean writeToDatabase(String firstname, String lastname, String mail, String password, String tickets){
        Connection conn = null;
        Statement stmt = null;
        try {
            //Connect to databse.
            conn = database.getConnection();
            stmt = conn.createStatement();

            // create table if not exist
            String createTable = "CREATE TABLE IF NOT EXISTS USERS " +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " FIRSTNAME           TEXT    NOT NULL, " +
                    " LASTNAME           TEXT    NOT NULL, " +
                    " MAIL           TEXT    NOT NULL, " +
                    " PASSWORD        CHAR(20) NOT NULL," +
                    " TICKETS            INT )";
            stmt.executeUpdate(createTable);

            // input values to database
            PreparedStatement pstmt = conn.prepareStatement
                    ("insert into USERS(FIRSTNAME, LASTNAME, MAIL, PASSWORD, TICKETS) values(?,?,?,?,?)");
            int i = 1;
            pstmt.setString(i++, firstname);
            pstmt.setString(i++, lastname);
            pstmt.setString(i++, mail);
            pstmt.setString(i++, password);
            pstmt.setInt(i++, Integer.parseInt(tickets));
            pstmt.executeUpdate();

            //Close the connection.
            stmt.close();
            conn.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            latestError = "Not possible to insert in database.";
            return false;
        }
        database.shutdown();
        return true;
    }

    /**
     * Encrypt the password.
     * @param password Value from password field.
     * @return Encrypted password if success, null otherwise.
     */
    private String encryptMD5(String password){
        //This is just basic encryption. Better improvements can be made using SALT.
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] passwordBytes = password.getBytes();
            md.reset();

            byte[] digested = md.digest(passwordBytes);
            StringBuffer sb = new StringBuffer();
            for(int i=0;i<digested.length;i++){
                sb.append(Integer.toHexString(0xff & digested[i]));
            }

            String encryptedPassword = sb.toString();
            System.out.println(encryptedPassword);
            return sb.toString();
        } catch (Exception e) {
            latestError = "Error while encrypting password.";
        }

        return null;
    }


}
