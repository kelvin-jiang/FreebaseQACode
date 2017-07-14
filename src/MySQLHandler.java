import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLHandler {
    private Connection connection;
    private ResultSet queryResult;

    public MySQLHandler(String URL, String user, String pass) {
        String dbDriver = "com.mysql.jdbc.Driver";
        try {
            Class.forName(dbDriver).newInstance();
            connection = DriverManager.getConnection(URL, user, pass);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    //---MYSQL METHODS---
    public void queryTable(String query) {
        try {
            queryResult = connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTable(String query) {
        try {
            connection.createStatement().executeUpdate(query);
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
            queryResult.previous(); //puts cursor right before first row, simulating a reset
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getQueryResult() {
        return queryResult;
    }

    //---MISCELLANEOUS METHODS---
    public String escapeMetaCharacters(String input) { //adds backslashes to escape special characters that MySQL cannot handle
        final String[] metaCharacters = {"\'", "\"", "\\"};
        String output = input;
        if (input != null) {
            for (String metaCharacter : metaCharacters) {
                if (input.contains(metaCharacter)) {
                    output = input.replace(metaCharacter, "\\" + metaCharacter);
                }
            }
        }
        return output;
    }
}
