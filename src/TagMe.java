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
    private double rhoThreshold;
    private WebClient client;
    private Page page;
    private JSONParser parser;
    private JSONObject annotationsObject;
    private JSONArray annotations;
    private Iterator iterator;
    private JSONObject annotationElement;
    private Map<String, String> tags;

    public TagMe(double threshold) {
        rhoThreshold = threshold;

        client = new WebClient();
        client.getOptions().setUseInsecureSSL(true);

        parser = new JSONParser();

        tags = new HashMap<>();
    }

    public void tag(String question) {
        tags.clear(); //clear previous tags
        String url = "https://tagme.d4science.org/tagme/tag?gcube-token=e276e0d3-30d5-4c40-bc43-4e19eafb1d89-843339462&text=".concat(question);

        try {
            page = client.getPage(url); //connects to specific TagMe page
            String json = page.getWebResponse().getContentAsString();

            annotationsObject = (JSONObject) parser.parse(json);

            annotations = (JSONArray) annotationsObject.get("annotations");
            iterator = annotations.iterator();
            int size = annotations.size();
            for (int i = 0; i < size; i++) {
                annotationElement = (JSONObject) iterator.next();

                double rho = (double) annotationElement.get("rho");
                if (rho > rhoThreshold) {
                    String tag = (String) annotationElement.get("title");
                    if (tag.contains("(") && tag.contains(")")) //chops brackets off if the tag has brackets at the end due to Wikipedia
                        tag = tag.substring(0, tag.indexOf("(") - 1);
                    String spot = (String) annotationElement.get("spot");
                    tags.put(tag.toLowerCase().trim(), spot);
                }
                iterator.remove();
            }
            client.close();
            annotationsObject.clear();
            annotations.clear();
            annotationElement.clear();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getTags() {
        return tags;
    }

}
