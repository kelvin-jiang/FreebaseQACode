import java.sql.SQLException;

public class LocalDBHandler extends MySQLHandler {
    private static final String DATABASE_NAME = "qatofreebase_db";
    private static final String TABLE_NAME = "`freebase_triples`";

    private static String dbURL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME;
    private static String dbUser = "root";
    private static String dbPass = "root";

    public LocalDBHandler() {
        super(dbURL, dbUser, dbPass);
        /*updateTable("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);

        updateTable("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(subjectID VARCHAR(255)," +
            "predicate VARCHAR(255)," +
            "objectID VARCHAR(255)," +
            "rowID INTEGER(11)" +
            "question VARCHAR(255)" +
            "answer VARCHAR(255)" +
            "yn CHAR(1) +" +
            "id INTEGER(11) AUTO_INCREMENT PRIMARY KEY)");*/
    }
    public void addRow(String subjectID, String predicate, String objectID, int rowID, String question, String answer, char yn) {
        updateTable(String.format("INSERT INTO %s (subjectID, subject, predicate, objectID, object, rowID, question, answer, yn) " +
                        "VALUES (%s, %s, %s, %d, %s, %s, %c)",
            String.format("%s.%s", DATABASE_NAME, TABLE_NAME), subjectID, predicate, objectID, rowID, question, answer, yn));
    }

    public String retrieveRow(String searchTerm) {
        queryTable("SELECT * FROM " + String.format("%s.%s", DATABASE_NAME, TABLE_NAME) +
            " WHERE `subjectID` = '" + searchTerm + "'");
        String row = "";
        try {
            while (getQueryResult().next()){
                row = String.format("%s \t %s \t %s \t %d \t %s \t %s \t %c \t %d", getQueryResult().getString("subjectID"),
                    getQueryResult().getString("predicate"), getQueryResult().getString("objectID"),
                    getQueryResult().getInt("rowID"), getQueryResult().getString("question"),
                    getQueryResult().getString("answer"), getQueryResult().getString("yn").charAt(0),
                    getQueryResult().getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    public void updateYN(int id, char yn) {
        updateTable(String.format("UPDATE %s SET `yn` = '%c' WHERE `id` = %d",
            String.format("%s.%s", DATABASE_NAME, TABLE_NAME), yn, id));
    }
}
