
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
        /*db.queryTable("SELECT * FROM freebase_mysql_db.`freebase-onlymid_-_fb-id2row-id` WHERE `count_row_id` = 1085");
        System.out.println(db.parseQueryResult(4));*/

        String freebaseID = db.getFreebaseIDs("York University").get(0);
        System.out.println(freebaseID);
        System.out.println(db.getFreebaseRowIDs(freebaseID));
    }
}
