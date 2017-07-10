import java.util.Arrays;
import java.util.List;

public class Main {
    private static final int TEST_INDEX = 3;

    public static void main(String[] args) {
        String FILEPATH = args[0]; //take command line argument as JSON file to be read

        //retrieve QA from JSON
        RetrieveQA retrieval = new RetrieveQA(FILEPATH);
        //int size = retrieval.getSize(); unused
        String[] questions = retrieval.getQuestions();
        String[] answers = retrieval.getAnswers();

        //tag Q
        String question = questions[TEST_INDEX];
        String answer = answers[TEST_INDEX];
        System.out.println(question + " (" + answer + ")");
        TagMe tagger = new TagMe(question);
        String[] questionTags = tagger.retrieveEntities();
        System.out.print("Tags: ");
        System.out.println(Arrays.toString(questionTags));

        //loop through all questions in JSON file
        /*for (String question : questions) {
            System.out.println(question);
            TagMe tagger = new TagMe(question);
            String[] questionTags = tagger.retrieveEntities();
            for (String questionTag : questionTags) {
                System.out.println(questionTag);
            }
            System.out.println("\n");
        }*/

        //Freebase
        FreebaseDBHandler db = new FreebaseDBHandler();
        //test query/update
        /*db.updateTable("ALTER TABLE freebase_mysql_db.`freebase-onlymid_-_id2en_name` ADD PRIMARY KEY (`freebase_mid`)");
        //System.out.println(db.parseQueryResult(2));*/

        //test methods
        /*final String TEST_SEARCH = "Colors (feat. Tatu)";
        System.out.println("Search Term: " + TEST_SEARCH);
        List<String> freebaseIDs = db.getFreebaseIDs(TEST_SEARCH);
        System.out.println("Machine IDs: " + freebaseIDs);
        String freebaseID = freebaseIDs.get(0);
        List<String> objects = db.getObjectsfromRowIDs(Long.parseLong(db.getFreebaseRowIDs(freebaseID).get(0)), Long.parseLong(db.getFreebaseRowIDs(freebaseID).get(1)));
        System.out.println("Machine IDs of Objects of \"" + TEST_SEARCH + "\": " + objects);
        System.out.println("Object Names: " + db.getNamesfromIDs(objects));*/

        //final String SEARCH = "Conro";
        for (String tag : questionTags) {
            List<String> freebaseIDs = db.getFreebaseIDs(tag);
            for (String freebaseID : freebaseIDs) {
                List<String> objects = db.getObjectsfromRowIDs(Long.parseLong(db.getFreebaseRowIDs(freebaseID).get(0)), Long.parseLong(db.getFreebaseRowIDs(freebaseID).get(1)));
                List<String> freebaseAnswers = db.getNamesfromIDs(objects);
                System.out.println("Freebase Answers: " + freebaseAnswers);
            }
        }
    }
}
