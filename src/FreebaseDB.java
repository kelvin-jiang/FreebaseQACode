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
    private final String NAME2ID_TABLE_NAME = "`freebase-onlymid_-_en_name2id`";
    private final String ALIAS2ID_TABLE_NAME = "`freebase-onlymid_-_en_alias2id`";
    private final String ID2NAME_TABLE_NAME = "`freebase-onlymid_-_id2en_name`";

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

    //---common methods---
    public void queryTable(String query) {
        try {
            queryResult = connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //parse and return a range of columns up to int numOfColumns (starts from 1)
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

    //parse and return a single column, int column (starts from 1)
    public List<String> singleQueryResult(int column) {
        List<String> resultList = new ArrayList<>();
        try {
            while (queryResult.next()) {
                resultList.add(queryResult.getString(column));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public void resetQueryCursor(){
        try {
            queryResult.first(); //puts cursor at first row
            queryResult.previous(); //puts cursor right before first row, simulating reset
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //---table-specific methods---
    //search term -> freebase IDs
    public List<String> getFreebaseIDs(String searchTerm) {
        List<String> freebaseIDs = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s WHERE `en_name` LIKE '%s'",
                String.format("%s.%s", DATABASE_NAME, NAME2ID_TABLE_NAME),
                String.format("\"%s\"@en", searchTerm)));

        List<String> searchResults = singleQueryResult(2);
        freebaseIDs.addAll(Arrays.asList(searchResults.get(0).split(",")));
        return freebaseIDs;
    }

    //freebase ID -> freebase row IDs
    public List<String> getFreebaseRowIDs(String freebaseID) {
        List<String> rowIDs = new ArrayList<>();

        //chop off brackets with number of facts at the end of freebaseID
        freebaseID = freebaseID.substring(0, freebaseID.indexOf("("));

        queryTable(String.format("SELECT * FROM %s WHERE `freebase_id` = '<http://rdf.freebase.com/ns/%s>'",
                String.format("%s.%s", DATABASE_NAME, ROWIDS_TABLE_NAME), freebaseID));

        List<String> searchResults = singleQueryResult(2); //min rowID
        rowIDs.add(searchResults.get(0));
        resetQueryCursor();
        searchResults = singleQueryResult(3); //max rowID
        rowIDs.add(searchResults.get(0));

        return rowIDs;
    }

    //freebase row IDs -> objects
    public List<String> getObjectsfromID(long minRowID, long maxRowID) {
        List<String> objects = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s WHERE `row_id` BETWEEN %d AND %d",
                String.format("%s.%s", DATABASE_NAME, DATADUMP_TABLE_NAME), minRowID, maxRowID));

        List<String> searchResults = singleQueryResult(3);
        for (String searchResult : searchResults) {
            System.out.println(searchResult);
            if (searchResult.contains("<http://rdf.freebase.com/ns/m."))
                objects.add(searchResult.substring(28, searchResult.length() - 1)); //only adds objects that are freebase IDs
        }
        return objects;
    }

    //freebase ID -> name
    public String getNamefromID(String freebaseID) {
        String name = "";

        queryTable(String.format("SELECT * FROM %s WHERE `freebase_mid` = '%s'",
                String.format("%s.%s", DATABASE_NAME, ID2NAME_TABLE_NAME), freebaseID));

        List<String> searchResults = singleQueryResult(2);
        for (String searchResult : searchResults) {
            if (searchResult.contains("@en"))
                name = searchResult.substring(searchResult.indexOf("\""), searchResult.lastIndexOf("\""));
        }
        return name;
    }
}
