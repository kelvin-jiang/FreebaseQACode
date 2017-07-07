import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FreebaseDB {
    private final String DATABASE_NAME = "freebase_mysql_db";
    private final String DATADUMP_TABLE_NAME = "`freebase-onlymid_-_datadump`";
    private final String ROWIDS_TABLE_NAME = "`freebase-onlymid_-_fb-id2row-id`";
    private final String NAMEIDS_TABLE_NAME = "`freebase-onlymid_-_en_name2id`";
    private final String ALIASIDS_TABLE_NAME = "`freebase-onlymid_-_en_alias2id`";

    private final String dbDriver = "com.mysql.jdbc.Driver";
    private final String dbURL = "jdbc:mysql://image.eecs.yorku.ca:3306/" + DATABASE_NAME +
        "?autoReconnect=true&useSSL=false";
    private final String dbUser = "read_only_user";
    private final String dbPass = "P@ssw0rd";

    private Connection connection;
    private ResultSet queryResult;

    public FreebaseDB() {
        try {
            Class.forName(dbDriver).newInstance();
            connection = DriverManager.getConnection(dbURL, dbUser, dbPass);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /*public FreebaseDB(String driver, String URL, String user, String pass) {
        dbDriver = driver;
        dbURL = URL;
        dbUser = user;
        dbPass = pass;
        connection = connectDatabase(dbDriver, dbURL, dbUser, dbPass);
        //query = null;
    }*/

    //if there is another use for this code, uncomment and use the method (currently used in the constructor only)
    /*private Connection connectDatabase(String driver, String URL, String user, String pass) {
        try {
            Class.forName(driver).newInstance();
            return DriverManager.getConnection(URL, user, pass);
        } catch (IllegalAccessException | InstantiationException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    //common methods
    public void queryTable(String query) {
        try {
            queryResult = connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<List<String>> parseQueryResult(int numOfColumns) {
        List<List<String>> resultList = new ArrayList<>();
        try {
            while (queryResult.next()) {
                List<String> row = new ArrayList<>(numOfColumns);
                for (int i = 1; i <= numOfColumns; i++) {
                    row.add(queryResult.getString(i));
                }
                resultList.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    //table-specific methods
    public List<String> getFreebaseIDs(String searchTerm) {
        List<String> freebaseIDs = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s WHERE `en_name` LIKE '%s'",
            String.format("%s.%s", DATABASE_NAME, NAMEIDS_TABLE_NAME),
            String.format("\"%s\"@en", searchTerm)));

        List<List<String>> searchResults = parseQueryResult(2);
        for (List<String> searchResult : searchResults) {
            freebaseIDs.addAll(Arrays.asList(searchResult.get(1).split(",")));
        }
        return freebaseIDs;
    }

    public List<String> getFreebaseRowIDs(String freebaseID) {
        List<String> rowIDs = new ArrayList<>();

        //chop off brackets with number of facts at the end of freebaseID
        freebaseID = freebaseID.substring(0, freebaseID.indexOf("("));

        queryTable(String.format("SELECT * FROM %s WHERE `freebase_id` = '<http://rdf.freebase.com/ns/%s>'",
                String.format("%s.%s", DATABASE_NAME, ROWIDS_TABLE_NAME), freebaseID));

        List<List<String>> searchResults = parseQueryResult(4);
        rowIDs.add(searchResults.get(0).get(1)); //min rowID
        rowIDs.add(searchResults.get(0).get(2)); //max rowID
        return rowIDs;
    }
}
