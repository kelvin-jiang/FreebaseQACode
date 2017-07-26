//demo site -> https://tagme.d4science.org/tagme/
//this class uses the API provided by TagMe

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

public class TagMe {
    private static double rhoThreshold;
    private static WebClient client;
    private static Page page;
    private static JSONParser parser = new JSONParser();
    private static JSONObject json;
    private static JSONArray annotations;
    private static Iterator iterator;
    private static JSONObject annotationElement;
    private static Object rhoValue = new Object();
    private static Map<String, String> tags = new HashMap<>();

    public static void setRhoThreshold(double threshold) {
        rhoThreshold = threshold;
    }

    public static void startWebClient() {
        client = new WebClient();
        client.getOptions().setUseInsecureSSL(true);
    }

    public static void tag(String question) {
        tags.clear(); //clear previous tags
        String url = "https://tagme.d4science.org/tagme/tag?gcube-token=e276e0d3-30d5-4c40-bc43-4e19eafb1d89-843339462&text=".concat(question);

        try {
            page = client.getPage(url); //connects to specific TagMe page
            String jsonString = page.getWebResponse().getContentAsString();

            json = (JSONObject) parser.parse(jsonString);
            annotations = (JSONArray) json.get("annotations");
            iterator = annotations.iterator();

            int size = annotations.size();
            for (int i = 0; i < size; i++) {
                //get an annotation
                annotationElement = (JSONObject) iterator.next();
                rhoValue = annotationElement.get("rho");
                double rho;
                if (rhoValue instanceof  Long) //when rho = 1 from TagMe, it becomes an instanceof Long which cannot be cast
                    rho = ((Long) rhoValue).doubleValue();
                else
                    rho = (double) rhoValue;

                if (rho > rhoThreshold) {
                    String tag = (String) annotationElement.get("title");
                    if (tag != null) {
                        if (tag.contains("(") && tag.contains(")")) //chops brackets off if the tag has brackets at the end due to Wikipedia
                            if (tag.indexOf("(") != 0)
                                tag = tag.substring(0, tag.indexOf("(") - 1);
                        tag = tag.toLowerCase().trim();
                    }
                    String spot = (String) annotationElement.get("spot");
                    tags.put(tag, spot);
                }
                iterator.remove();
            }
            client.close();
            json.clear();
            annotations.clear();
            annotationElement.clear();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getTags() {
        return tags;
    }

}
