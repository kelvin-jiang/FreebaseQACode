import java.sql.SQLException;

public class MatchesDBHandler extends MySQLHandler {
    private static final String DATABASE_NAME = "Kelvin_MatchesDB";
    private static final String TABLE_NAME = "`FreebaseQA_Matches`";

    public MatchesDBHandler(String dbURL, String dbUser, String dbPass) {
        super(dbURL, dbUser, dbPass);
    }

    public void addRow(String subject, String subjectID, String predicate, String mediatorPredicate, String objectID, String object, String question,
                       int rating) {
        updateTable(String.format("INSERT INTO %s.%s (`subject`, `subjectID`, `predicate`, `mediator_predicate`, `objectID`, " +
                "`object`, `question`, `rating`) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', %d)", DATABASE_NAME, TABLE_NAME,
                subject, subjectID, predicate, mediatorPredicate, objectID, object, question, rating));
    }

    public String retrieveRow(int searchID) {
        queryTable(String.format("SELECT * FROM %s.%s WHERE `ID` = %d", DATABASE_NAME, TABLE_NAME, searchID));
        String row = "";
        try {
            while (getQueryResult().next()){
                row = String.format("%s   %s   %s   %s   %s   %s   %s   %d   %d", getQueryResult().getString("subject"),
                        getQueryResult().getString("subjectID"), getQueryResult().getString("predicate"),
                        getQueryResult().getString("mediator_predicate"), getQueryResult().getString("objectID"),
                        getQueryResult().getString("object"), getQueryResult().getString("question"),
                        getQueryResult().getInt("rating"), getQueryResult().getInt("ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    public void updateRating(int rating, int ID) {
        updateTable(String.format("UPDATE %s.%s SET `rating` = %d WHERE `id` = %d", DATABASE_NAME, TABLE_NAME, rating, ID));
    }
}
