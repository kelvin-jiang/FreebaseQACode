import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QARetrieval {
    private static JSONParser parser = new JSONParser();
    private static JSONObject json;
    private static JSONArray data;
    private static Iterator iterator;
    private static JSONObject dataElement;
    private static JSONObject answerElement;
    private static List<String> questions = new ArrayList<>();
    private static List<String> answers = new ArrayList<>();

    public static void parseJSON(String filepath) {
        try {
            json = (JSONObject) parser.parse(new FileReader(filepath));
            data = (JSONArray) json.get("Data");
            iterator = data.iterator();

            int size = data.size();
            for (int i = 0; i < size; i++) {
                //get a QA set
                dataElement = (JSONObject) iterator.next();
                //get the answer
                answerElement = (JSONObject) dataElement.get("Answer");
                String answer = (String) answerElement.get("MatchedWikiEntityName");
                answer = answer != null ? answer : (String) answerElement.get("NormalizedValue"); //if MatchedWikiEntityName is null, use NormalizedValue
                if (answer != null) //if either MatchedWikiEntityName or NormalizedValue returns a value
                    answers.add(answer.toLowerCase().trim());
                //get the question
                String question = (String) dataElement.get("Question");
                questions.add(question);

                iterator.remove();
            }
            answerElement.clear();
            dataElement.clear();
            data.clear();
            json.clear();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getQuestions() {
        return questions;
    }

    public static List<String> getAnswers() {
        return answers;
    }
}
