import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLHandler {
    private Connection connection;
    private Statement statement;
    private ResultSet queryResult;

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    List<List<String>> result2DList = new ArrayList<>(); //parseQueryResult
    List<String> resultList = new ArrayList<>(); //singleQueryResult
    String output; //escapeMetaCharacters

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
            statement = connection.createStatement();
            queryResult = statement.executeQuery(query);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTable(String query) {
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //parse and return a range of columns up to int numOfColumns (starts from 1)
    public List<List<String>> parseQueryResult(int numOfColumns) {
        result2DList.clear();
        try {
            while (queryResult.next()) {
                List<String> row = new ArrayList<>(numOfColumns);
                for (int i = 1; i <= numOfColumns; i++) {
                    row.add(queryResult.getString(i));
                }
                result2DList.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result2DList;
    }

    //parse and return a single column, int column (starts from 1)
    public List<String> singleQueryResult(int column) {
        resultList.clear();
        try {
            while (queryResult.next()) {
                resultList.add(queryResult.getString(column));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    //parse and return two columns, int column1 and column2 (starts from 1)
    public List<List<String>> doubleQueryResult(int column1, int column2) {
        list1.clear();
        list2.clear();
        result2DList.clear();
        try {
            while (queryResult.next()) {
                list1.add(queryResult.getString(column1));
            }
            result2DList.add(list1);
            resetQueryCursor();
            while (queryResult.next()) {
                list2.add(queryResult.getString(column2));
            }
            result2DList.add(list2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result2DList;
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
        output = input;
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
