import java.sql.*;

public class MySQL_DB {
    private final String DATABASE_NAME = "QAToFreebase_db";
    private final String TABLE_NAME = "freebase_triples";

    private String dbDriver = "com.mysql.jdbc.Driver";
    private String dbURL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME;
    private String dbUser = "root";
    private String dbPass = "root";

    private Connection connection;
    private ResultSet queryResult;

    public MySQL_DB() {
        connection = connectDatabase(dbDriver, dbURL, dbUser, dbPass);
            String command = "CREATE TABLE " + TABLE_NAME +
                    "(subjectID VARCHAR(255)," +
                    "predicate VARCHAR(255)," +
                    "objectID VARCHAR(255)," +
                    "rowID INTEGER" +
                    "question VARCHAR(255)" +
                    "answer VARCHAR(255)" +
                    "yn CHAR(1))";

            updateTable(command);
    }

    /*public MySQL_DB(String driver, String URL, String user, String pass) {
        dbDriver = driver;
        dbURL = URL;
        dbUser = user;
        dbPass = pass;
        connection = connectDatabase(dbDriver, dbURL, dbUser, dbPass);
        //query = null;
    }*/

    private Connection connectDatabase(String driver, String URL, String user, String pass) {
        try {
            Class.forName(driver).newInstance();
            return DriverManager.getConnection(URL, user, pass);
        } catch (IllegalAccessException | InstantiationException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet queryTable(String query) {
        try {
            Statement statement = connection.createStatement();
            queryResult = statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return queryResult;
    }

    public void updateTable(String query) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRow(String subjectID, String predicate, String objectID, int rowID, String question, String answer, char yn){
        updateTable("INSERT INTO " + TABLE_NAME + " VALUES ('" + subjectID + "', '" + predicate + "', '" + objectID +
            "', " + rowID + ", '" + question + "', '" + answer + "', '" + yn + "')");
    }

    public String retrieveRow(String searchTerm){
        queryResult = queryTable("SELECT * FROM " + TABLE_NAME + " WHERE subjectID = '" + searchTerm + "'");
        String row = "";
        try {
            while (queryResult.next()){
                row = row.concat(queryResult.getString("subjectID")).concat("\t").concat(queryResult.getString("predicate"))
                    .concat("\t").concat(queryResult.getString("objectID")).concat("\t").concat(Integer.toString(queryResult.getInt("rowID"))
                    .concat("\t").concat(queryResult.getString("question")).concat("\t").concat(queryResult.getString("answer"))
                    .concat("\t").concat(queryResult.getString("yn")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }
}
