# Event Registration
A single root page website containing a section with information about an Icecream Festival and a registration form.
The front-end is implemented using the Polymer framework. The Java Play framework was used for the back-end.

## Project Structure
### Front-end
- **Public > app** contains *index.html*, which uses *event-information.html* and *event-registeration.html*.
- **Public > components** contains the Polymer components, like paper-button.
- **Public > images** contains the icon for the Ice Cream Festival.
- **Public > stylesheets** own .css files are found here.
- **Public > themes** the Polymer Melon theme was chosen.

### Back-end
- **conf > application.conf**: the path to the database is specified here.
- **conf > routes**: end-points of the Restfull API.
- **app > data**: contains the SQLite Databse.
- **app > controllers**:  implementation of the registration can be found here.

### General
- **bower.json** : dependencies regarding the polymer web components are specified here.
- **build.sbt** : general dependencies.
- **.bowerrcc**: directory containing the web components is specified here.

## Run the project
- **bower install** installs the required Polymer web components.
-  In the application directory, type **activator** to open the Play console. Type **run** and open a web browser on the indicated URL. (localhost:9000)
