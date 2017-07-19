import java.util.*;

public class Main {
    //---VARIABLES FOR PROCESSING COMMAND LINE ARGUMENTS---
    private static String filepath;
    private static int startIndex = 0;
    private static int endIndex = 1000000; //arbitrary value
    private static double rhoThreshold = 0.2;

    public static void main(String[] args) {
        //---OBJECTS AND FIELDS---
        QARetrieval retrieval = new QARetrieval();
        String[] questionBank, answerBank;
        String question, answer;
        TagMe tagger;
        FreebaseDBHandler db = new FreebaseDBHandler();
        Set<String> tags = new HashSet<>(); //uses a hashset to ensure unique tags
        Set<String> tagIDs = new HashSet<>();
        List<NTriple> tagTriples = new ArrayList<>();
        Map<String, NTriple> mediatorTriples = new HashMap<>();
        NTriple mediatorTriple;
        List<NTriple> answerTriples = new ArrayList<>();
        Set<String> answerIDs = new HashSet<>();
        Set<List<String>> matches = new HashSet<>(); //matches are saved uniquely based on subject, predicate, mediatorPredicate, object
        List<String> match = new ArrayList<>();

        //---FUNCTIONS---
        processArguments(args);

        retrieval.parseJSON(filepath); //retrieves QAs from the JSON file
        questionBank = retrieval.getQuestions();
        answerBank = retrieval.getAnswers();

        tagger = new TagMe(rhoThreshold); //sets up the TagMe tagger

        if (startIndex < 0) startIndex = 0; //ensures startIndex has a minimum value of 0
        if (endIndex < startIndex || endIndex > questionBank.length) endIndex = questionBank.length; //ensures endIndex cannot be less than startIndex
        for (int i = startIndex; i < endIndex; i++) {
            question = questionBank[i];
            answer = answerBank[i];
            System.out.println(question + " (" + answer + ")");

            if (question == null || answer == null) continue; //skips the QA pair if Q or A is null

            tags.addAll(tagger.tag(question));
            if (tags.size() != 0)
                tags.remove(answer.toLowerCase().trim()); //removes tags that are equivalent to the answer
            if (tags.size() == 0) continue; //skips the QA pair if there are no tags
            System.out.println("TAGS: " + tags);

            //bottom-up
            answerIDs = db.nameAlias2IDs(answer, answerIDs); //prepares all freebase IDs with a name or alias matching the answer
            for (String answerID : answerIDs) {
                answerTriples = db.ID2Triples(answerID, answerTriples);
                for (NTriple answerTriple : answerTriples) {
                    if (db.isIDMediator(answerTriple.getObjectID()))
                        mediatorTriples.put(answerTriple.getObjectID(), answerTriple);
                }
                answerTriples.clear();
            }

            //top-down
            for (String tag : tags) {
                tagIDs = db.nameAlias2IDs(tag, tagIDs);
                for (String tagID : tagIDs) {
                    tagTriples = db.ID2Triples(tagID, tagTriples);
                    for (NTriple triple : tagTriples) {
                        if (answerIDs.contains(triple.getObjectID())) { //if the object of the triple has an ID matching an answer ID
                            triple.setSubject(tag);
                            triple.setObject(answer);
                            match.add(triple.getSubject());
                            match.add(triple.getPredicate());
                            match.add(null);
                            match.add(triple.getObject());
                            if (!matches.contains(match)) {
                                matches.add(match);
                                System.out.printf("MATCHED1: %s | %s | %s\n", triple.toString(), question, answer);
                                System.out.printf("PROCESSED %d QUESTIONS WITH %d MATCHES\n", i - startIndex + 1, matches.size());
                            }
                        }
                        else if (mediatorTriples.containsKey(triple.getObjectID())) { //if the object of the triple has an ID matching a mediator
                            triple.setSubject(tag); //no object name for triple because mediator
                            mediatorTriple = mediatorTriples.get(triple.getObjectID());
                            mediatorTriple.setSubject(answer); //no object name for mediatorTriple because mediator
                            match.add(triple.getSubject());
                            match.add(triple.getPredicate());
                            match.add(mediatorTriple.getPredicate());
                            match.add(mediatorTriple.getSubject());
                            if (!matches.contains(match)) {
                                matches.add(match);
                                System.out.printf("MATCHED2: %s | %s | %s | %s\n", triple.toString(), mediatorTriple.toReverseString(), question, answer);
                                System.out.printf("PROCESSED %d QUESTIONS WITH %d MATCHES\n", i - startIndex + 1, matches.size());
                            }
                        }
                        match.clear();
                    }
                    tagTriples.clear();
                }
                tagIDs.clear();
            }
            mediatorTriples.clear();
            answerIDs.clear();
            tags.clear();
            System.gc(); //prompts Java's garbage collector to clean up data structures
        }
        System.out.println("PROCESSING COMPLETE\nNUMBER OF MATCHES: " + matches.size());
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
