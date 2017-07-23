import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class QARetrieval {
    private JSONParser parser;
    private JSONObject json;
    private JSONArray data;
    private Iterator iterator;
    private JSONObject dataElement;
    private JSONObject answerObject;
    private String[] questions;
    private String[] answers;

    public QARetrieval() {}

    public void parseJSON(String filepath) {
        try {
            parser = new JSONParser();
            json = (JSONObject) parser.parse(new FileReader(filepath));
            data = (JSONArray) json.get("Data");
            int size = data.size();
            iterator = data.iterator();

            answers = new String[size];
            questions = new String[size];

            for (int i = 0; i < size; i++) {
                //get a QA set
                dataElement = (JSONObject) iterator.next();

                //get the answer
                answerObject = (JSONObject) dataElement.get("Answer");
                String answer = (String) answerObject.get("MatchedWikiEntityName");
                answer = answer != null ? answer : (String) answerObject.get("NormalizedValue"); //if MatchedWikiEntityName is null, use NormalizedValue
                if (answer != null) //if either MatchedWikiEntityName or NormalizedValue returns a value
                    answers[i] = answer.toLowerCase().trim();

                //get the question
                String question = (String) dataElement.get("Question");
                questions[i] = question;

                iterator.remove();
            }
            json.clear();
            data.clear();
            dataElement.clear();
            answerObject.clear();
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
