import java.util.List;

public class Main {
    //private static final int TEST_INDEX = 211;

    public static void main(String[] args) {
        /*String FILEPATH = args[0]; //take command line argument as JSON file to be read

        //retrieve QA from JSON
        RetrieveQA retrieval = new RetrieveQA(FILEPATH);
        //int size = retrieval.getSize(); unused
        String[] questions = retrieval.getQuestions();
        String[] answers = retrieval.getAnswers();

        //tag Q
        for (String question : questions) {
            System.out.println(question);
            TagMe tagger = new TagMe(question);
            String[] questionTags = tagger.retrieveEntities();
            for (String questionTag : questionTags) {
                System.out.println(questionTag);
            }
            System.out.println("\n");
        }*/

        //MySQL
        FreebaseDB db = new FreebaseDB();

        //test query
        /*db.queryTable("SELECT * FROM freebase_mysql_db.`freebase-onlymid_-_id2en_name`" +
                " WHERE `freebase_mid` LIKE 'm.0y988qx'");
        System.out.println(db.parseQueryResult(2));*/

        String freebaseID = db.getFreebaseIDs("Conro").get(0);
        System.out.println(freebaseID);
        List<String> objects = db.getObjectsfromID(Long.parseLong(db.getFreebaseRowIDs(freebaseID).get(0)), Long.parseLong(db.getFreebaseRowIDs(freebaseID).get(1)));
        System.out.println(objects);
        System.out.println(objects.get(0));
        System.out.println(db.getNamefromID(objects.get(0)));
    }
}
