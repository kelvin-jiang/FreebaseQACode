import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class RetrieveQA {
    private int size;
    private String[] questions;
    private String[] answers;

    public RetrieveQA(String filepath) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(new FileReader(filepath));

            JSONArray data = (JSONArray) jsonData.get("Data");
            size = data.size();
            questions = new String[size];
            answers = new String[size];
            Iterator dataIterator = data.iterator();

            for (int i = 0; i < size; i++) {
                //get a QA set
                JSONObject jsonDataElement = (JSONObject) dataIterator.next();

                //get the answer
                JSONObject jsonAnswer = (JSONObject) jsonDataElement.get("Answer");
                String answer = (String) jsonAnswer.get("MatchedWikiEntityName");
                answers[i] = answer;

                //get the question
                String question = (String) jsonDataElement.get("Question");
                questions[i] = question;
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public int getSize() {
        return size;
    }

    public String[] getQuestions() {
        return questions;
    }

    public String[] getAnswers() {
        return answers;
    }
}
