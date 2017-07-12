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

        for (String tag : questionTags) { //repeats for each tag
            List<String> tagIDs = db.nameAlias2IDs(tag);
            for (String tagID : tagIDs) { //repeats for each tag ID
                List<NTriple> triples = db.ID2Triples(tagID);
                for (NTriple triple : triples) { //repeats for each triple
                    List<String> namesAliases = db.objectID2NamesAliases(triple.getFormattedObjectID()); //gets names/aliases from object of current triple
                    triple.setSubject(tag); //adds subject name to triple
                    for (String nameAlias : namesAliases) { //repeats for each name/alias
                        triple.setObject(nameAlias); //adds object name to triple
                        System.out.println(triple.toString());
                        if (nameAlias.toLowerCase().trim().equals(answer.toLowerCase().trim())) {
                            String[] saveData = {triple.getSubject(), triple.getSubjectID(), triple.getFormattedPredicate(), triple.getFormattedObjectID(),
                                    triple.getObject(), question, answer};
                            System.out.println("TO SAVE: " + Arrays.toString(saveData));
                        }
                    }
                }
            }
        }
    }
}
