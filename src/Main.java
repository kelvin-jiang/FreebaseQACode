//put -verbose:gc in VM options in Configurations to print GC data

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    //---STATIC VARIABLES---
    private static String filepath;
    private static boolean isRetrieved = false;
    private static int startIndex = 0;
    private static int endIndex = Integer.MAX_VALUE; //arbitrary value
    private static double rhoThreshold = 0.2;
    private static List<String> questionBank = new ArrayList<>();
    private static List<String> answerBank = new ArrayList<>();

    public static void main(String[] args) {
        //---LOCAL OBJECTS AND FIELDS---
        QARetrieval retrieval = new QARetrieval();
        String question, answer;
        TagMe tagger;
        FreebaseDBHandler db = new FreebaseDBHandler();
        List<String> IDsList = new ArrayList<>(); //placeholder list for nameAlias2IDs method
        Map<String, String> tags = new HashMap<>(); //uses a hash structure to ensure unique tags
        String spot; //stores a tag's corresponding spot when the tag get removed
        Set<String> tagIDs = new HashSet<>();
        List<NTriple> tagTriples = new ArrayList<>();
        Map<String, NTriple> mediatorTriples = new HashMap<>();
        NTriple mediatorTriple;
        List<NTriple> answerTriples = new ArrayList<>();
        Set<String> answerIDs = new HashSet<>();
        Set<List<String>> matches = new HashSet<>(); //matches are saved uniquely based on subject, predicate, mediatorPredicate, object
        List<String> match = new ArrayList<>();
	    boolean matched;
	    int uniqueMatches = 0;
	    long startTime = System.currentTimeMillis();
	    long previousTime = System.currentTimeMillis();

        //---FUNCTIONS---
        processArguments(args);

        if (isRetrieved) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
                String line;
                String[] qa;
                while ((line = reader.readLine()) != null) { //reads all QA lines from text file
                    qa = line.split(" \\| ");
                    questionBank.add(qa[0]);
                    answerBank.add(qa[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else {
            retrieval.parseJSON(filepath); //retrieves QAs from the JSON file
            questionBank = retrieval.getQuestions();
            answerBank = retrieval.getAnswers();
        }

        tagger = new TagMe(rhoThreshold); //sets up the TagMe tagger

        if (startIndex < 0) startIndex = 0; //ensures startIndex has a minimum value of 0
        if (endIndex < startIndex || endIndex > questionBank.size()) endIndex = questionBank.size(); //ensures endIndex cannot be less than startIndex
        for (int i = startIndex; i < endIndex; i++) {
            question = questionBank.get(i);
            answer = answerBank.get(i);
            System.out.printf("%d. %s (%s)\n", i+1, question, answer);
	    
	        matched = false;

            if (question == null || answer == null) continue; //skips the QA pair if Q or A is null

            tagger.tag(question);
            tags.putAll(tagger.getTags());
            if (tags.size() != 0) {
                spot = tags.remove(answer.toLowerCase().trim()); //removes tags that are equivalent to the answer
                if (spot != null) { //if a tag was removed, the collected spot is used as the tag
                    tags.put(spot.toLowerCase().trim(), spot.toLowerCase().trim());
                    tags.remove(answer.toLowerCase().trim()); //in case the spot is also equivalent to the answer
                }
            }
            if (tags.size() == 0) {
                System.out.println(); //prints an empty line for spacing
                continue; //skips the QA pair if there are no tags to use
            }
            System.out.println("TAGS: " + tags);

            //bottom-up
            answerIDs = db.nameAlias2IDs(answer, IDsList, answerIDs); //prepares all freebase IDs with a name or alias matching the answer
            for (String answerID : answerIDs) {
                answerTriples = db.ID2Triples(answerID, answerTriples);
                if (answerTriples == null)
                    continue;
                for (NTriple answerTriple : answerTriples) {
                    if (db.isIDMediator(answerTriple.getObjectID()))
                        mediatorTriples.put(answerTriple.getObjectID(), answerTriple);
                }
                answerTriples.clear();
            }

            //top-down
            for (String tag : tags.keySet()) {
                tagIDs = db.nameAlias2IDs(tag, IDsList, tagIDs);
                for (String tagID : tagIDs) {
                    tagTriples = db.ID2Triples(tagID, tagTriples);
                    if (tagTriples == null)
                        continue;
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
                                matched = true;
				                System.out.printf("MATCHED1: %s | %s | %s\n", triple.toString(), question, answer);
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
				                matched = true;
                                System.out.printf("MATCHED2: %s | %s | %s | %s\n", triple.toString(), mediatorTriple.toReverseString(), question, answer);
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
            IDsList.clear();
            tags.clear();
            System.gc(); //prompts Java's garbage collector to clean up data structures
            if (matched) uniqueMatches++;
            System.out.printf("PROGRESS: %d MATCHES (%d UNIQUE MATCHES)\nTIME: %.3fs FOR QUESTION AND %.3fs SINCE START\n\n",
                    matches.size(), uniqueMatches, (System.currentTimeMillis() - previousTime)/1000.0, (System.currentTimeMillis() - startTime)/1000.0);
            previousTime = System.currentTimeMillis();
        }
        System.out.printf("PROCESSING COMPLETE\nRESULTS: %d MATCHES (%d UNIQUE MATCHES)\n", matches.size(), uniqueMatches);
        matches.clear(); //can't be too safe
    }

    private static void processArguments(String[] args) {
        if (args.length > 4) {
            System.out.printf("USAGE: \tjava Main [path to .JSON or .TXT file]\n\tjava Main [path to .JSON or .TXT file] [start index]\n\t" +
                    "java Main [path to .JSON or .TXT file] [start index] [end index]\n\tjava Main [path to .JSON or .TXT file] [start index] " +
                    "[end index] [rho threshold]\n");
            System.exit(1);
        }
        if (args.length >= 2) {
            startIndex = Integer.parseInt(args[1]);
            if (args.length >= 3) {
                endIndex = Integer.parseInt(args[2]);
                if (args.length == 4)
                    rhoThreshold = Double.parseDouble(args[3]);
            }
        }
        filepath = args[0];
        if (args[0].contains(".txt"))
            isRetrieved = true;
    }
}
