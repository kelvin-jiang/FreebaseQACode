import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final int TEST_INDEX = 2;
    private static final double RHO_THRESHOLD = 0.15;

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
        TagMe tagger = new TagMe(question, RHO_THRESHOLD);
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

        List<String> freebaseAnswers = new ArrayList<>(); //not really needed, just to output

        for (String tag : questionTags) { //repeat for each tag
            List<String> tagIDs = db.getIDs(tag); //tag name/alias -> tag ID
            for (String tagID : tagIDs) { //repeat for each tag ID
                List<String> objectIDs = db.getObjects(db.getRowIDs(tagID)); //tag ID -> tag row IDs -> object IDs
                for (String objectID : objectIDs) { //repeat for each object ID
                    List<String> nameAliasRowIDs = db.getNameAliasRowIDs(db.getRowIDs(objectID)); //object ID -> object row IDs -> object name/alias row IDs
                    for (String nameAliasRowID : nameAliasRowIDs) { //repeat for each name/alias row ID
                        List<String> nameAliasRowIDList = new ArrayList<>(); //getObjects only accepts arrays, so a length 1 list is created
                        nameAliasRowIDList.add(nameAliasRowID);
                        List<String> namesAliases = db.getObjects(nameAliasRowIDList);
                        for (String nameAlias : namesAliases) {
                            freebaseAnswers.add(nameAlias);
                            if (nameAlias.toLowerCase().trim().equals(answer.toLowerCase().trim())) {
                                List<String> saveData = new ArrayList<>();
                                saveData.addAll(db.collectTriplet(nameAliasRowID)); //adds the subject-predicate-object triplet to saveData
                                saveData.add(question);
                                saveData.add(answer);
                                System.out.println("TO SAVE: " + saveData);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Freebase Answers: " + freebaseAnswers);
    }
}
