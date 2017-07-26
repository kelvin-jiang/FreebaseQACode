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
    private JSONParser parser;
    private JSONObject json;
    private JSONArray data;
    private Iterator iterator;
    private JSONObject dataElement;
    private JSONObject answerElement;
    private List<String> questions;
    private List<String> answers;

    public QARetrieval() {
        parser = new JSONParser();
        questions = new ArrayList<>();
        answers = new ArrayList<>();
    }

    public void parseJSON(String filepath) {
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

    public List<String> getQuestions() {
        return questions;
    }

    public List<String> getAnswers() {
        return answers;
    }
}
