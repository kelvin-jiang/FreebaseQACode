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

    private static final String dbURL = "jdbc:mysql://image.eecs.yorku.ca:3306/" + DATABASE_NAME +
        "?autoReconnect=true&useSSL=false";
    private static final String dbUser = "read_only_user";
    private static final String dbPass = "P@ssw0rd";

    public FreebaseDBHandler() {
        super(dbURL, dbUser, dbPass);
    }

    //---COMMON METHODS---
    //freebase ID -> freebase row IDs
    public List<String> ID2RowIDs(String freebaseID) {
        List<String> rowIDs = new ArrayList<>();

        //searches in the form of <http://rdf.freebase.com/ns/ID>
        queryTable(String.format("SELECT * FROM %s.%s WHERE `freebase_id` = '<http://rdf.freebase.com/ns/%s>'",
                DATABASE_NAME, ROWIDS_TABLE_NAME, freebaseID));

        List<String> searchResults = singleQueryResult(2);
        if (searchResults.size() != 0) {
            rowIDs.add(searchResults.get(0)); //min rowID
            resetQueryCursor();
            searchResults = singleQueryResult(3);
            rowIDs.add(searchResults.get(0)); //max rowID
        }
        return rowIDs;
    }

    //---SEQUENTIAL METHODS---
    //name/alias -> freebase IDs
    public List<String> nameAlias2IDs(String tag) {
        List<String> freebaseIDs = new ArrayList<>();

        //searches in the form of "name"@en
        queryTable(String.format("SELECT * FROM %s.%s WHERE `en_name` = '\"%s\"@en'",
                DATABASE_NAME, NAME2ID_TABLE_NAME, tag));
        freebaseIDs = collectIDs(freebaseIDs);

        //searches in the form of "alias"@en
        queryTable(String.format("SELECT * FROM %s.%s WHERE `en_alias` = '\"%s\"@en'",
                DATABASE_NAME, ALIAS2ID_TABLE_NAME, tag));
        freebaseIDs = collectIDs(freebaseIDs);

        return freebaseIDs;
    }
    private List<String> collectIDs(List<String> freebaseIDs) {
        List<String> searchResults = singleQueryResult(2);
        if (searchResults.size() != 0) { //ensures that searchResults is not empty
            for (String searchResult : searchResults) {
                for (String ID : Arrays.asList(searchResult.split(","))) { //splits the searchResult into individual IDs in a list
                    ID = ID.substring(0, ID.indexOf("(")); //chops off number of facts in brackets at the end of the ID
                    freebaseIDs.add(ID);
                }
            }
        }
        return freebaseIDs;
    }

    //freebase ID -> (row IDs) -> (object freebase IDs) -> NTriples
    public List<NTriple> ID2Triples(String freebaseID) {
        List<String> rowIDs = ID2RowIDs(freebaseID); //gets a range (a minimum and a maximum) of rows of the freebase ID
        List<NTriple> triples = new ArrayList<>();

        //searches for all objectIDs from range of rows to get objectIDs that are freebase IDs
        queryTable(String.format("SELECT * FROM %s.%s WHERE `row_id` BETWEEN %d AND %d",
                DATABASE_NAME, DATADUMP_TABLE_NAME, Long.parseLong(rowIDs.get(0)), Long.parseLong(rowIDs.get(1))));

        List<String> predicates = singleQueryResult(2);
        resetQueryCursor();
        List<String> objects = singleQueryResult(3);
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).contains("<http://rdf.freebase.com/ns/m.")) { //only adds objectIDs that are freebase IDs
                triples.add(new NTriple(null, freebaseID, predicates.get(i), objects.get(i), null)); //null values to be filled out in the main method
            }
        }
        return triples;
    }

    //object freebase ID -> (row IDs) -> object name/aliases
    public List<String> objectID2NamesAliases(String freebaseID) {
        List<String> namesAliasesRowIDs = new ArrayList<>();
        List<String> namesAliases = new ArrayList<>();

        List<String> objectRowIDs = ID2RowIDs(freebaseID);
        //searches for all predicates and corresponding row IDs from range of rows to get name/alias predicates
        queryTable(String.format("SELECT * FROM %s.%s WHERE `row_id` BETWEEN %d AND %d",
                DATABASE_NAME, DATADUMP_TABLE_NAME, Long.parseLong(objectRowIDs.get(0)), Long.parseLong(objectRowIDs.get(1))));

        List<String> predicates = singleQueryResult(2);
        resetQueryCursor();
        List<String> rowIDs = singleQueryResult(4);
        if (predicates.size() != 0) { //ensures that predicate is not empty
            for (int i = 0; i < predicates.size(); i++) {
                if (predicates.get(i).equals("<http://rdf.freebase.com/ns/type.object.name>") || predicates.get(i).equals("<http://rdf.freebase.com/ns/common.topic.alias>")) {
                    namesAliasesRowIDs.add(rowIDs.get(i)); //saves row ID of the matching predicate
                }
            }
        }
        //searches for the object of each row ID to get the name/alias of the original object
        for (String namesAliasesRowID : namesAliasesRowIDs) { //repeats for each row ID
            queryTable(String.format("SELECT * FROM %s.%s WHERE `row_id` = %d",
                    DATABASE_NAME, DATADUMP_TABLE_NAME, Long.parseLong(namesAliasesRowID)));

            List<String> searchResults = singleQueryResult(3);
            for (String searchResult: searchResults) {
                if (searchResult.endsWith("@en")) { //adds only english names and aliases
                    namesAliases.add(searchResult.substring(searchResult.indexOf("\"") + 1, searchResult.lastIndexOf("\""))); //extracts name/alias
                }
            }
        }
        return namesAliases;
    }
}
