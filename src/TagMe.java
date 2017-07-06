//https://tagme.d4science.org/tagme/ demo site
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

public class TagMe {
    private final double RHO_THRESHOLD = 0.1;
    private String json;

    public TagMe(String question){
        String url = "https://tagme.d4science.org/tagme/tag?gcube-token=e276e0d3-30d5-4c40-bc43-4e19eafb1d89-843339462&text="
                .concat(question);

        try {
            //connect to tagme website
            final WebClient client = new WebClient();
            client.getOptions().setUseInsecureSSL(true);

            final Page page = client.getPage(url);
            //copy tagme's output json data
            json = page.getWebResponse().getContentAsString();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] retrieveEntities() {
        ArrayList<String> tags = new ArrayList();

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonAnnotations = (JSONObject) parser.parse(json);

            JSONArray annotations = (JSONArray) jsonAnnotations.get("annotations");
            Iterator annotationsIterator = annotations.iterator();

            for (int i = 0; i < annotations.size(); i++) {
                JSONObject jsonAnnotationElement = (JSONObject) annotationsIterator.next();

                double rho = (double) jsonAnnotationElement.get("rho");
                if (rho > RHO_THRESHOLD)
                    tags.add((String) jsonAnnotationElement.get("title"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tags.toArray(new String[tags.size()]);
    }
}
