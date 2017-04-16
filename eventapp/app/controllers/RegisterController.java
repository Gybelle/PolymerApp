package controllers;

import play.mvc.*;

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

        System.out.println(firstname);

        //Validation
        boolean namesOk = validateNames(firstname, lastname);
        boolean mailOk = validateMail(mail);
        boolean passOk = validatePassword(password);
        boolean ticketsOk = validate(tickets);

        if (!namesOk || !mailOk || !passOk || !ticketsOk){
            return badRequest("Could not submit registration: " + latestError);
        }

        boolean toDatabaseOk = writeToDatabase(firstname, lastname, mail, password, tickets);

        return ok("Successful Registration");
    }

    private String extractTextField(Map<String, String[]> data, String fieldname){
        try {
            String fieldValue = data.get(fieldname)[0];
            System.out.println(fieldname + ": " + fieldValue);
            return fieldValue;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private boolean validate(String field){
        return field != null;
    }

    private boolean validateNames(String firstname, String lastname){
        Pattern pattern;
        Matcher matcherFN, matcherLN;
        boolean firstnameOk = validate(firstname);
        boolean lastnameOk = validate(lastname);

        if(!firstnameOk || !lastnameOk){
            latestError = "Firstname and lastname are required.";
            return false;
        }

        return true;
    }

    private boolean validateMail(String mail){
        boolean mailOk = validate(mail);
        Pattern pattern;
        Matcher matcher;

        if(!mailOk){
            latestError = "Mail is required.";
            return false;
        }

        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(mail);
        mailOk = matcher.matches();

        if(!mailOk){
            latestError = "Bad mail format.";
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password){
        boolean passwordOk = validate(password);

        if(!passwordOk){
            latestError = "Password is required.";
            return false;
        }
        return true;
    }

    private boolean writeToDatabase(String firstname, String lastname, String mail, String password, String tickets){
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = database.getConnection();
            stmt = conn.createStatement();

            // create table if not exist
            String createTable = "CREATE TABLE IF NOT EXISTS USERS " +
                    "(ID INT PRIMARY KEY AUTOINCREMENT," +
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

            stmt.close();
            conn.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        database.shutdown();
        return true;
    }


}
