import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class QARetrievalMain {
    private static String filepath;

    public static void main(String[] args) {
        processArguments(args);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("libs/data.txt"));

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new FileReader(filepath)); //first argument is filepath to JSON file
            JSONArray data = (JSONArray) json.get("Data");
            Iterator iterator = data.iterator();
            int size = data.size();

            JSONObject dataElement = new JSONObject();
            JSONObject answerElement = new JSONObject();
            for (int i = 0; i < size; i++) {
                //get a QA set
                dataElement = (JSONObject) iterator.next();
                //get the answer
                answerElement = (JSONObject) dataElement.get("Answer");
                String answer = (String) answerElement.get("MatchedWikiEntityName");
                answer = answer != null ? answer : (String) answerElement.get("NormalizedValue"); //if MatchedWikiEntityName is null, use NormalizedValue
                if (answer != null) //if either MatchedWikiEntityName or NormalizedValue returns a value
                    answer = answer.toLowerCase().trim();
                //get the question
                String question = (String) dataElement.get("Question");

                writer.write(question + " | " + answer);
                writer.newLine();

                iterator.remove();
            }
            writer.close();

            answerElement.clear();
            dataElement.clear();
            data.clear();
            json.clear();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("RETRIEVAL COMPLETE");
    }

    private static void processArguments(String[] args) {
        if (args.length != 1) {
            System.out.println("USAGE: java QARetrievalMain [path to .JSON file]");
        }
        else filepath = args[0];
    }
}
