import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Main {
    private static final double RHO_THRESHOLD = 0.15;

    public static void main(String[] args) {
        String FILEPATH = args[0]; //take command line argument as JSON file to be read
        int matches = 0;

        //retrieve QA from JSON
        RetrieveQA retrieval = new RetrieveQA(FILEPATH);
        String[] questions = retrieval.getQuestions();
        String[] answers = retrieval.getAnswers();

        for (int i = 0; i < 50; i++) {
            String question = questions[i];
            String answer = answers[i];
            System.out.println(question + " (" + answer + ")");
            if (question == null || answer == null) {
                continue; //skip the QA pair if Q or A is null
            }
            //tag Q
            TagMe tagger = new TagMe(question, RHO_THRESHOLD);
            HashSet<String> questionTags = new HashSet<>(Arrays.asList(tagger.retrieveEntities())); //uses a hashset to ensure unique tags
            if (questionTags.size() != 0)
                questionTags.remove(answer.toLowerCase().trim()); //removes tags that are equivalent to the answer

            System.out.print("Tags: ");
            System.out.println(questionTags);

            //Freebase
            FreebaseDBHandler db = new FreebaseDBHandler();

            //top-down + bottom-up search
            HashSet<String> answerIDs = new HashSet<>();
            answerIDs.addAll(db.nameAlias2IDs(answer)); //prepares all freebase IDs with a name or alias matching the answer

            for (String tag : questionTags) {
                List<String> tagIDs = db.nameAlias2IDs(tag);
                for (String tagID : tagIDs) {
                    List<NTriple> triples = db.ID2Triples(tagID);
                    for (NTriple triple : triples) {
                        if (answerIDs.contains(triple.getObjectID())) { //if the object of the triple has an ID matching an answer ID, check its name
                            List<String> namesAliases = db.objectID2NamesAliases(triple.getObjectID()); //gets names/aliases from object of current triple
                            triple.setSubject(tag);
                            for (String nameAlias : namesAliases) { //repeats for each name/alias
                                triple.setObject(nameAlias); //adds object name to triple
                                if (nameAlias.toLowerCase().trim().equals(answer.toLowerCase().trim())) {
                                    String[] saveData = {triple.getSubject(), triple.getSubjectID(), triple.getPredicate(), triple.getObjectID(),
                                            triple.getObject(), question, answer};
                                    matches++;
                                    System.out.println("TO SAVE: " + Arrays.toString(saveData));
                                    break; //no need to run through all names/aliases of a single object after obtaining a match
                                }
                            }
                        }
                    }
                }
            }
            answerIDs.clear();

            //top-down search
        /*for (String tag : questionTags) { //repeats for each tag
            List<String> tagIDs = db.nameAlias2IDs(tag);
            for (String tagID : tagIDs) { //repeats for each tag ID
                List<NTriple> triples = db.ID2Triples(tagID);
                for (NTriple triple : triples) { //repeats for each triple
                    List<String> namesAliases = db.objectID2NamesAliases(triple.getObjectID()); //gets names/aliases from object of current triple
                    triple.setSubject(tag); //adds subject name to triple
                    for (String nameAlias : namesAliases) { //repeats for each name/alias
                        triple.setObject(nameAlias); //adds object name to triple
                        System.out.println(triple.toString());
                        if (nameAlias.toLowerCase().trim().equals(answer.toLowerCase().trim())) {
                            String[] saveData = {triple.getSubject(), triple.getSubjectID(), triple.getPredicate(), triple.getObjectID(),
                                    triple.getObject(), question, answer};
                            System.out.println("TO SAVE: " + Arrays.toString(saveData));
                        }
                    }
                }
            }
        }*/
        }
        System.out.println("Number of Matches: " + matches);
    }
}
