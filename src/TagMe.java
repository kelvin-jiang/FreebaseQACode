//demo site -> https://tagme.d4science.org/tagme/
//this class uses the API provided by tagme

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
    private double rhoThreshold;
    private WebClient client;

    public TagMe(double rho){
        rhoThreshold = rho;

        client = new WebClient(); //starts a WebClient
        client.getOptions().setUseInsecureSSL(true);
    }

    public List<String> tag(String question) {
        String url = "https://tagme.d4science.org/tagme/tag?gcube-token=e276e0d3-30d5-4c40-bc43-4e19eafb1d89-843339462&text="
                .concat(question);
        List<String> tags = new ArrayList<>();

        try {
            Page page = client.getPage(url); //connects to TagMe page
            String jsonOutput = page.getWebResponse().getContentAsString(); //copies TagMe's output json data

            JSONParser parser = new JSONParser();
            JSONObject jsonAnnotations = (JSONObject) parser.parse(jsonOutput);

            JSONArray annotations = (JSONArray) jsonAnnotations.get("annotations");
            Iterator annotationsIterator = annotations.iterator();

            for (int i = 0; i < annotations.size(); i++) {
                JSONObject jsonAnnotationElement = (JSONObject) annotationsIterator.next();

                double rho = (double) jsonAnnotationElement.get("rho");
                if (rho > rhoThreshold) {
                    String tag = (String) jsonAnnotationElement.get("title");
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
