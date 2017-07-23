import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class QARetrieval {
    private String filepath;
    private JSONParser parser;
    private JSONObject json;
    private JSONArray data;
    private int size;
    private Iterator iterator;
    private JSONObject dataElement;
    private JSONObject answerObject;
    private String question;
    private String answer;
    private String[] questions;
    private String[] answers;

    public QARetrieval() {}

    public void parseJSON(String filepath) {
        this.filepath = filepath;
        try {
            parser = new JSONParser();
            json = (JSONObject) parser.parse(new FileReader(this.filepath));
            data = (JSONArray) json.get("Data");
            size = data.size();
            iterator = data.iterator();

            answers = new String[size];
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
