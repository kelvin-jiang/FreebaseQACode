//demo site -> https://tagme.d4science.org/tagme/
//this class uses the API provided by TagMe

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TagMe {
    private double RHO_THRESHOLD;
    private WebClient client;
    private Page page;
    private String url;
    private String json;
    private JSONParser parser = new JSONParser();
    private JSONObject annotationsObject;
    private JSONArray annotations;
    private Iterator iterator;
    private JSONObject annotationElement;
    private double rho;
    private String tag;

    private List<String> tags = new ArrayList<>();

    public TagMe(double threshold) {
        RHO_THRESHOLD = threshold;

        client = new WebClient();
        client.getOptions().setUseInsecureSSL(true);
    }

    public List<String> tag(String question) {
        tags.clear(); //clear previous tags
        url = "https://tagme.d4science.org/tagme/tag?gcube-token=e276e0d3-30d5-4c40-bc43-4e19eafb1d89-843339462&text=".concat(question);

        try {
            page = client.getPage(url); //connects to specific TagMe page
            json = page.getWebResponse().getContentAsString(); //copies TagMe's output json data

            annotationsObject = (JSONObject) parser.parse(json);

            annotations = (JSONArray) annotationsObject.get("annotations");
            iterator = annotations.iterator();

            for (int i = 0; i < annotations.size(); i++) {
                annotationElement = (JSONObject) iterator.next();

                rho = (double) annotationElement.get("rho");
                if (rho > RHO_THRESHOLD) {
                    tag = (String) annotationElement.get("title");
                    if (tag.contains("(") && tag.contains(")")) //chops brackets off if the tag has brackets at the end due to Wikipedia
                        tag = tag.substring(0, tag.indexOf("(") - 1);
                    tags.add(tag.toLowerCase().trim());
                }
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return tags;
    }
}
