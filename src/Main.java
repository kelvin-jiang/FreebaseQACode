import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Main {
    private static final double RHO_THRESHOLD = 0.15;

    public static void main(String[] args) {
        String FILEPATH = args[0]; //takes command line argument as the filepath for the JSON file to be read

        RetrieveQA retrieval = new RetrieveQA(FILEPATH); //retrieve QA from the JSON file
        String[] questionBank = retrieval.getQuestions();
        String[] answerBank = retrieval.getAnswers();
        String question;
        String answer;

        TagMe tagger = new TagMe(RHO_THRESHOLD);
        HashSet<String> tags = new HashSet<>();

        FreebaseDBHandler db = new FreebaseDBHandler();
        HashSet<String> answerIDs = new HashSet<>(); //bottom-up
        List<String> tagIDs; //top-down
        List<NTriple> triples;
        List<String> namesAliases;

        int matches = 0;

        for (int i = 0; i < questionBank.length; i++) {
            question = questionBank[i];
            answer = answerBank[i];
            System.out.println(question + " (" + answer + ")");

            if (question == null || answer == null)
                continue; //skip the QA pair if Q or A is null

            tags.addAll(tagger.tag(question)); //uses a hashset to ensure unique tags
            if (tags.size() != 0)
                tags.remove(answer.toLowerCase().trim()); //removes tags that are equivalent to the answer
            System.out.println("Tags: " + tags);

            //top-down + bottom-up search
            answerIDs.addAll(db.nameAlias2IDs(answer)); //prepares all freebase IDs with a name or alias matching the answer

            for (String tag : tags) {
                tagIDs = db.nameAlias2IDs(tag);
                for (String tagID : tagIDs) {
                    triples = db.ID2Triples(tagID);
                    for (NTriple triple : triples) {
                        if (answerIDs.contains(triple.getObjectID())) { //if the object of the triple has an ID matching an answer ID, check its name
                            namesAliases = db.objectID2NamesAliases(triple.getObjectID()); //gets names/aliases from object of current triple
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
            tags.clear();
            answerIDs.clear();

            //top-down search
            /*for (String tag : tags) { //repeats for each tag
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
