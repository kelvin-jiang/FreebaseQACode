import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Main {
    private static final int TEST_INDEX = 37;
    private static final double RHO_THRESHOLD = 0.2;

    public static void main(String[] args) {
        String FILEPATH = args[0]; //take command line argument as JSON file to be read

        //retrieve QA from JSON
        RetrieveQA retrieval = new RetrieveQA(FILEPATH);
        //int size = retrieval.getSize(); //unused
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

        //top-down + bottom-up search
        //List<String> answerIDsList = db.nameAlias2IDs(answer);
        HashSet<String> answerIDs = new HashSet<>();
        answerIDs.addAll(db.nameAlias2IDs(answer)); //prepares all freebase IDs with a name or alias matching the answer
        System.out.println(answerIDs);

        for (String tag : questionTags) {
            List<String> tagIDs = db.nameAlias2IDs(tag);
            for (String tagID : tagIDs) {
                List<NTriple> triples = db.ID2Triples(tagID);
                for (NTriple triple : triples) {
                    if (answerIDs.contains(triple.getFormattedObjectID())) { //if the object of the triple has an ID matching an answer ID, check its name
                        List<String> namesAliases = db.objectID2NamesAliases(triple.getFormattedObjectID()); //gets names/aliases from object of current triple
                        triple.setSubject(tag);
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

        //top-down search
        /*for (String tag : questionTags) { //repeats for each tag
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
        }*/
    }
}
