import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Main {
    //---VARIABLES FOR PROCESSING COMMAND LINE ARGUMENTS---
    private static String filepath;
    private static int startIndex = 0;
    private static int endIndex = 1000000; //arbitrary value
    private static double rhoThreshold = 0.2;

    public static void main(String[] args) {
        //---VARIABLES AND OBJECTS, listed in the order they are used---
        QARetrieval retrieval = new QARetrieval();
        String[] questionBank, answerBank;
        String question, answer;
        TagMe tagger;
        HashSet<String> tags = new HashSet<>();
        FreebaseDBHandler db = new FreebaseDBHandler();
        HashSet<String> answerIDs = new HashSet<>();
        List<String> answerIDsList = new ArrayList<>(answerIDs); //answerIDsList is a copy of answerIDs in ArrayList form
        List<NTriple> answerTriples = new ArrayList<>();
        List<String> mediatorNamesAliasesRowIDs = new ArrayList<>();
        List<NTriple> mediatorTriples = new ArrayList<>();
        HashSet<String> mediatorIDs = new HashSet<>();
        List<String> tagIDs = new ArrayList<>();
        List<NTriple> triples = new ArrayList<>();

        int matches = 0;

        //---FUNCTIONS---
        processArguments(args);

        retrieval.parseJSON(filepath); //retrieves QAs from the JSON file
        questionBank = retrieval.getQuestions();
        answerBank = retrieval.getAnswers();

        tagger = new TagMe(rhoThreshold); //sets up the TagMe tagger

        //checks the user-inputted bounds of the outermost loop, startIndex and endIndex
        if (startIndex < 0) startIndex = 0; //startIndex has a minimum value of 0
        if (endIndex < startIndex || endIndex > questionBank.length) endIndex = questionBank.length; //endIndex cannot be less than startIndex
        for (int i = startIndex; i < endIndex; i++) {
            question = questionBank[i];
            answer = answerBank[i];
            System.out.println(question + " (" + answer + ")");

            if (question == null || answer == null) continue; //skips the QA pair if Q or A is null

            tags.addAll(tagger.tag(question)); //uses a hashset to ensure unique tags
            if (tags.size() != 0)
                tags.remove(answer.toLowerCase().trim()); //removes tags that are equivalent to the answer
            if (tags.size() == 0) continue; //skips the QA pair if there are no tags
            System.out.println("Tags: " + tags);

            //bottom-up
            answerIDs.addAll(db.nameAlias2IDs(answer, answerIDsList)); //prepares all freebase IDs with a name or alias matching the answer
            for (String answerID : answerIDs) {
                answerTriples = db.ID2Triples(answerID, answerTriples);
                mediatorTriples.addAll(db.triples2Mediators(answerTriples, mediatorNamesAliasesRowIDs, mediatorTriples));
                //mediatorIDs.addAll(db.ID2MediatorIDs(answerID, answerObjectIDs, answerObjectNamesAliasesRowIDs, answerObjectNamesAliases, mediatorIDsList));
            }
            for (NTriple mediatorTriple : mediatorTriples) {
                mediatorIDs.add(mediatorTriple.getObjectID());
            }

            //top-down
            for (String tag : tags) {
                tagIDs = db.nameAlias2IDs(tag, tagIDs);
                for (String tagID : tagIDs) {
                    triples = db.ID2Triples(tagID, triples);
                    for (NTriple triple : triples) {
                        if (answerIDs.contains(triple.getObjectID())) { //if the object of the triple has an ID matching an answer ID, check its name
                            //namesAliases = db.objectID2NamesAliases(triple.getObjectID(), namesAliasesRowIDs, namesAliases); //gets names/aliases from object of current triple
                            triple.setSubject(tag);
                            //for (String nameAlias : namesAliases) {
                                //if (nameAlias.toLowerCase().trim().equals(answer.toLowerCase().trim())) { //if the name/alias matches the answer, save all data
                                    triple.setObject(answer);
                                    //triple.setObject(nameAlias); //adds object name to triple
                                    matches++;
                                    System.out.printf("TO SAVE: %s     %s     %s\n", triple.toString(), question, answer);
                                    System.out.printf("PROCESSED %d QUESTIONS WITH %d MATCHES\n", i - startIndex + 1, matches);
                                    //break; //no need to run through all names/aliases of a single object after obtaining a match
                                //}
                            //}
                            //namesAliasesRowIDs.clear();
                            //namesAliases.clear();
                        }
                        else if (mediatorIDs.contains(triple.getObjectID())) {
                            triple.setSubject(tag); //no object name for triple because mediator
                            for (NTriple mediatorTriple : mediatorTriples) { //go through mediator triples to find the corresponding one
                                if (mediatorTriple.getObjectID().equals(triple.getObjectID())) {
                                    mediatorTriple.setSubject(answer); //no object name for mediatorTriple because mediator
                                    matches++;
                                    System.out.printf("TO SAVE: %s     %s     %s     %s\n", triple.toString(), mediatorTriple.toReverseString(), question, answer);
                                    System.out.printf("PROCESSED %d QUESTIONS WITH %d MATCHES\n", i - startIndex + 1, matches);
                                    break; //ugly solution since there can be duplicates in mediatorTriples
                                }
                            }
                        }
                    }
                    triples.clear();
                }
                tagIDs.clear();
            }
            tags.clear();
            answerIDs.clear();
            answerIDsList.clear();
            answerTriples.clear();
            mediatorNamesAliasesRowIDs.clear();
            mediatorTriples.clear();
            mediatorIDs.clear();

            System.gc(); //prompts Java's garbage collector to clean up the cleared Lists and HashSets
        }
        System.out.println("Number of Matches: " + matches);
    }

    private static void processArguments(String[] args) {
        if (args.length == 2 || args.length > 4) {
            System.out.printf("USAGE: \tjava Main QA_FILE.json\n\tjava Main QA_FILE.json startIndex endIndex" +
                    "\n\tjava Main QA_FILE.json startIndex endIndex rhoThreshold\n");
            System.exit(1);
        }
        if (args.length >= 3) {
            startIndex = Integer.parseInt(args[1]);
            endIndex = Integer.parseInt(args[2]);
            if (args.length == 4) rhoThreshold = Double.parseDouble(args[3]);
        }
        filepath = args[0]; //takes command line argument as the filepath for the JSON file to be read
    }
}
