import java.io.*;
import java.util.Map;

public class TagMeMain extends TagMe {
    private static final double rhoThreshold = 0.2;
    private static String filepath;

    public static void main(String[] args) {
        processArguments(args);

        startWebClient();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(filepath.substring(0, filepath.indexOf(".")) + "-TagMe.txt"));
            String line;
            String[] qa;
            Map<String, String> tags;
            while ((line = reader.readLine()) != null) { //reads all QA lines from text file
                qa = line.split(" \\| ");
                writer.write(qa[0] + " | " + qa[1]);
                TagMe.tag(qa[0]);
                tags = TagMe.getTags();
                if (tags.size() != 0) {
                    for (String tag : tags.keySet()) {
                        writer.write(" | " + tag + " | " + tags.get(tag));
                    }
                }
                writer.newLine();
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("TAGME TAGGING COMPLETE");
    }

    private static void processArguments(String[] args) {
        if (args.length == 0 || args.length > 2) {
            System.out.println("USAGE: java TagMeMain [path to .TXT file]\n\tjava TagMeMain [path to .TXT file] [rho threshold]");
            System.exit(1);
        }
        if (args.length == 2)
            setRhoThreshold(Double.parseDouble(args[1]));
        else
            setRhoThreshold(rhoThreshold);
        filepath = args[0];
    }
}
