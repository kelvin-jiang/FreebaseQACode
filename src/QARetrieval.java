import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class QARetrieval {
    private String[] questions;
    private String[] answers;

    public QARetrieval(String filepath) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new FileReader(filepath));
            JSONArray data = (JSONArray) json.get("Data");
            int size = data.size();
            Iterator iterator = data.iterator();

            JSONObject dataElement;
            JSONObject answerObject;
            String answer;
            answers = new String[size];
            String question;
            questions = new String[size];

            for (int i = 0; i < size; i++) {
                //get a QA set
                dataElement = (JSONObject) iterator.next();

                //get the answer
                answerObject = (JSONObject) dataElement.get("Answer");
                answer = (String) answerObject.get("MatchedWikiEntityName");
                answer = answer != null ? answer : (String) answerObject.get("NormalizedValue"); //if MatchedWikiEntityName is null, use NormalizedValue
                if (answer != null) //if either MatchedWikiEntityName or NormalizedValue returns a value
                    answers[i] = answer.toLowerCase().trim();

                //get the question
                question = (String) dataElement.get("Question");
                questions[i] = question;
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getQuestions() {
        return questions;
    }

    public String[] getAnswers() {
        return answers;
    }
}
