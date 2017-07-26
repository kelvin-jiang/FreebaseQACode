import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class QARetrievalMain extends QARetrieval {
    private static String filepath;

    public static void main(String[] args) {
        processArguments(args);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filepath.substring(0, filepath.indexOf(".")) + "-data.txt"));

            parseJSON(filepath);
            List<String> questions = getQuestions();
            List<String> answers = getAnswers();

            for (int i = 0; i < questions.size(); i++) {
                writer.write(questions.get(i) + " | " + answers.get(i));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("RETRIEVAL COMPLETE");
    }

    private static void processArguments(String[] args) {
        if (args.length != 1) {
            System.out.println("USAGE: java QARetrievalMain [path to .JSON file]");
            System.exit(1);
        }
        else filepath = args[0];
    }
}
