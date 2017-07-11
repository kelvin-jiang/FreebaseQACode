import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FreebaseDBHandler extends MySQLHandler {
    //database information
    private static final String DATABASE_NAME = "freebase_mysql_db";
    private static final String DATADUMP_TABLE_NAME = "`freebase-onlymid_-_datadump`";
    private static final String ROWIDS_TABLE_NAME = "`freebase-onlymid_-_fb-id2row-id`";
    private static final String NAME2ID_TABLE_NAME = "`freebase-onlymid_-_en_name2id`";
    private static final String ALIAS2ID_TABLE_NAME = "`freebase-onlymid_-_en_alias2id`";
    private static final String ID2NAME_TABLE_NAME = "`freebase-onlymid_-_id2en_name`";
    private static final String ID2ALIAS_TABLE_NAME = "`freebase-onlymid_-_id2en_alias`";

    private static final String dbURL = "jdbc:mysql://image.eecs.yorku.ca:3306/" + DATABASE_NAME +
        "?autoReconnect=true&useSSL=false";
    private static final String dbUser = "read_only_user";
    private static final String dbPass = "P@ssw0rd";

    public FreebaseDBHandler() {
        super(dbURL, dbUser, dbPass);
    }

    //search term (name or alias) -> freebase IDs
    public List<String> getFreebaseIDs(String searchTerm) {
        List<String> freebaseIDs = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s WHERE `en_name` LIKE '%s'",
                String.format("%s.%s", DATABASE_NAME, NAME2ID_TABLE_NAME),
                String.format("\"%s\"@en", searchTerm)));
        freebaseIDs = collectIDs(freebaseIDs);

        queryTable(String.format("SELECT * FROM %s WHERE `en_alias` LIKE '%s'",
                String.format("%s.%s", DATABASE_NAME, ALIAS2ID_TABLE_NAME),
                String.format("\"%s\"@en", searchTerm)));
        freebaseIDs = collectIDs(freebaseIDs);

        return freebaseIDs;
    }

    private List<String> collectIDs(List<String> freebaseIDs) {
        List<String> searchResults = singleQueryResult(2);
        if (searchResults.size() != 0) {
            for (String searchResult : searchResults) {
                freebaseIDs.addAll(Arrays.asList(searchResult.split(",")));
            }
        }
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

    //freebase row IDs -> objects freebase IDs
    public List<String> getObjectIDsFromRowIDs(List<String> rowIDs) {
        List<String> objects = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s WHERE `row_id` BETWEEN %d AND %d",
                String.format("%s.%s", DATABASE_NAME, DATADUMP_TABLE_NAME),
                Long.parseLong(rowIDs.get(0)), Long.parseLong(rowIDs.get(1))));

        List<String> searchResults = singleQueryResult(3);
        for (String searchResult : searchResults) {
            if (searchResult.contains("<http://rdf.freebase.com/ns/m.")) //only adds objects that are freebase IDs
                objects.add(searchResult.substring(28, searchResult.length() - 1)); //cuts off URL portion of string
        }
        return objects;
    }

    //freebase ID -> name (name or alias)
    /*public List<String> getNamesfromIDs(List<String> freebaseIDs) {
        List<String> names = new ArrayList<>();

        for (String freebaseID : freebaseIDs) {
            queryTable(String.format("SELECT * FROM %s WHERE `freebase_mid` = '%s'",
                    String.format("%s.%s", DATABASE_NAME, ID2NAME_TABLE_NAME), freebaseID));
            names = collectNamesOrAliases(names);

            queryTable(String.format("SELECT * FROM %s WHERE `freebase_mid` = '%s'",
                    String.format("%s.%s", DATABASE_NAME, ID2ALIAS_TABLE_NAME), freebaseID));
            names = collectNamesOrAliases(names);
        }
        return names;
    }*/

    //freebase row IDs -> objects -> english names and aliases
    public List<String> getNamesFromRowIDs(List<String> rowIDs) {
        List<String> names = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s WHERE `row_id` BETWEEN %d AND %d",
                String.format("%s.%s", DATABASE_NAME, DATADUMP_TABLE_NAME),
                Long.parseLong(rowIDs.get(0)), Long.parseLong(rowIDs.get(1))));

        List<String> predicates = singleQueryResult(2);
        resetQueryCursor();
        List<String> objects = singleQueryResult(3);
        List<String> allNames = new ArrayList<>();
        if (predicates.size() != 0) {
            for (int i = 0; i < predicates.size(); i++) {
                if (predicates.get(i).equals("<http://rdf.freebase.com/ns/type.object.name>") ||
                        predicates.get(i).equals("<http://rdf.freebase.com/ns/common.topic.alias>")) {
                    allNames.add(objects.get(i)); //saves corresponding object
                }
            }
        }
        for (String name : allNames) {
            if (name.endsWith("@en"))
                names.add(name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\"")));
        }
        return names;
    }

    /*private List<String> collectNamesOrAliases(List<String> names) {
        List<String> searchResults = singleQueryResult(2);
        if (searchResults.size() != 0) {
            for (String searchResult : searchResults) { //there can be multiple names or aliases returned
                names.add(searchResult.substring(searchResult.indexOf("\"") + 1, searchResult.lastIndexOf("\"")));
            }
        }
        return names;
    }*/
}
