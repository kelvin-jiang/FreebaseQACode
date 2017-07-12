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
    //private static final String ID2NAME_TABLE_NAME = "`freebase-onlymid_-_id2en_name`";
    //private static final String ID2ALIAS_TABLE_NAME = "`freebase-onlymid_-_id2en_alias`";

    private static final String dbURL = "jdbc:mysql://image.eecs.yorku.ca:3306/" + DATABASE_NAME +
        "?autoReconnect=true&useSSL=false";
    private static final String dbUser = "read_only_user";
    private static final String dbPass = "P@ssw0rd";

    public FreebaseDBHandler() {
        super(dbURL, dbUser, dbPass);
    }

    //name/alias -> freebase IDs
    public List<String> getIDs(String searchTerm) {
        List<String> freebaseIDs = new ArrayList<>();

        //searches in the form of "name"@en
        queryTable(String.format("SELECT * FROM %s.%s WHERE `en_name` = '\"%s\"@en'",
                DATABASE_NAME, NAME2ID_TABLE_NAME, searchTerm));
        freebaseIDs = collectIDs(freebaseIDs);

        //searches in the form of "alias"@en
        queryTable(String.format("SELECT * FROM %s.%s WHERE `en_alias` = '\"%s\"@en'",
                DATABASE_NAME, ALIAS2ID_TABLE_NAME, searchTerm));
        freebaseIDs = collectIDs(freebaseIDs);

        return freebaseIDs;
    }
    private List<String> collectIDs(List<String> freebaseIDs) {
        List<String> searchResults = singleQueryResult(2);
        if (searchResults.size() != 0) { //ensures that searchResults is not empty
            for (String searchResult : searchResults) {
                //List<String> IDs = Arrays.asList(searchResult.split(","));
                for (String ID : Arrays.asList(searchResult.split(","))) { //splits the searchResult into individual IDs in a list
                    ID = ID.substring(0, ID.indexOf("(")); //chops off number of facts in brackets at the end of the ID
                    freebaseIDs.add(ID);
                }
            }
        }
        return freebaseIDs;
    }

    //freebase ID -> freebase row IDs
    public List<String> getRowIDs(String freebaseID) {
        List<String> rowIDs = new ArrayList<>();

        /*//chop off brackets with number of facts at the end of freebaseID
        if (freebaseID.indexOf("(") != -1) //only apply substring if brackets can be found
            freebaseID = freebaseID.substring(0, freebaseID.indexOf("("));*/

        //searches in the form of <http://rdf.freebase.com/ns/ID>
        queryTable(String.format("SELECT * FROM %s.%s WHERE `freebase_id` = '<http://rdf.freebase.com/ns/%s>'",
                DATABASE_NAME, ROWIDS_TABLE_NAME, freebaseID));

        List<String> searchResults = singleQueryResult(2); //min rowID
        rowIDs.add(searchResults.get(0));
        resetQueryCursor();
        searchResults = singleQueryResult(3); //max rowID
        rowIDs.add(searchResults.get(0));

        return rowIDs;
    }

    //freebase row IDs -> datadump(3) aka object
    public List<String> getObjects(List<String> rowIDs) {
        List<String> objects = new ArrayList<>();
        List<String> searchResults = new ArrayList<>();

        if (rowIDs.size() == 1) { //get object ENGLISH NAME/ALIAS from row
            queryTable(String.format("SELECT * FROM %s.%s WHERE `row_id` = %d",
                    DATABASE_NAME, DATADUMP_TABLE_NAME, Long.parseLong(rowIDs.get(0))));
             searchResults = singleQueryResult(3);
             for (String searchResult: searchResults) {
                 if (searchResult.endsWith("@en")) { //only adds english names and aliases
                     objects.add(searchResult.substring(searchResult.indexOf("\"") + 1, searchResult.lastIndexOf("\""))); //extracts name/alias
                 }
             }
        }
        else { //get all object IDS from range of rows
            queryTable(String.format("SELECT * FROM %s.%s WHERE `row_id` BETWEEN %d AND %d",
                    DATABASE_NAME, DATADUMP_TABLE_NAME, Long.parseLong(rowIDs.get(0)), Long.parseLong(rowIDs.get(1))));

            searchResults = singleQueryResult(3);
            for (String searchResult : searchResults) {
                if (searchResult.contains("<http://rdf.freebase.com/ns/m.")) //only adds objects that are freebase IDs
                    objects.add(searchResult.substring(28, searchResult.length() - 1)); //extracts freebase ID from URL
            }
        }
        return objects;
    }

    //row IDs -> row ID via datadump(2) aka predicate
    public List<String> getNameAliasRowIDs(List<String> inputRowIDs) {
        List<String> nameAliasRowIDs = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s.%s WHERE `row_id` BETWEEN %d AND %d",
                DATABASE_NAME, DATADUMP_TABLE_NAME, Long.parseLong(inputRowIDs.get(0)), Long.parseLong(inputRowIDs.get(1))));

        List<String> predicates = singleQueryResult(2);
        resetQueryCursor();
        List<String> rowIDs = singleQueryResult(4);
        if (predicates.size() != 0) { //ensures that predicate is not empty
            for (int i = 0; i < predicates.size(); i++) {
                if (predicates.get(i).equals("<http://rdf.freebase.com/ns/type.object.name>") ||
                        predicates.get(i).equals("<http://rdf.freebase.com/ns/common.topic.alias>")) {
                    nameAliasRowIDs.add(rowIDs.get(i)); //saves row ID of the matching predicate
                }
            }
        }
        return nameAliasRowIDs;
    }

    public List<String> collectTriplet(String rowID) {
        List<String> triplet = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s.%s WHERE `row_id` = %d",
                DATABASE_NAME, DATADUMP_TABLE_NAME, Long.parseLong(rowID)));

        triplet = parseQueryResult(3).get(0);
        return triplet;
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
    }

    //freebase row IDs -> objects -> row IDs of english names and aliases
    public List<String> getNameRowIDFromRowIDs(List<String> objectRowIDs) {
        //List<String> names = new ArrayList<>();
        List<String> nameRowIDs = new ArrayList<>();

        queryTable(String.format("SELECT * FROM %s WHERE `row_id` BETWEEN %d AND %d",
                String.format("%s.%s", DATABASE_NAME, DATADUMP_TABLE_NAME),
                Long.parseLong(objectRowIDs.get(0)), Long.parseLong(objectRowIDs.get(1))));

        List<String> predicates = singleQueryResult(2);
        resetQueryCursor();
        //List<String> objects = singleQueryResult(3);
        List<String> rowIDs = singleQueryResult(4);
        if (predicates.size() != 0) {
            for (int i = 0; i < predicates.size(); i++) {
                if (predicates.get(i).equals("<http://rdf.freebase.com/ns/type.object.name>") ||
                        predicates.get(i).equals("<http://rdf.freebase.com/ns/common.topic.alias>")) {
                    nameRowIDs.add(rowIDs.get(i)); //saves row ID with the matching predicate
                }
            }
        }
        return nameRowIDs;
    }

    //row IDs of english names and aliases -> predicates OR english names and aliases
    public String getDataFromRowID(int column, String rowID) {
        String name;

        queryTable(String.format("SELECT * FROM %s WHERE `row_id` = %d",
                String.format("%s.%s", DATABASE_NAME, DATADUMP_TABLE_NAME), Long.parseLong(rowID)));

        name = singleQueryResult(column).get(0);
        if (column == 2) { //when predicate
            name = name.substring(25, name.length() - 1);//chop off URL portion of string
        }
        else { //when object
            if (name.endsWith("@en"))
                name = name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\"")); //chop off @en suffix
        }

        return name;
    }

    private List<String> collectNamesOrAliases(List<String> names) {
        List<String> searchResults = singleQueryResult(2);
        if (searchResults.size() != 0) {
            for (String searchResult : searchResults) { //there can be multiple names or aliases returned
                names.add(searchResult.substring(searchResult.indexOf("\"") + 1, searchResult.lastIndexOf("\"")));
            }
        }
        return names;
    }*/
}
