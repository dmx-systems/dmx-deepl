package systems.dmx.deepl;

import systems.dmx.core.osgi.PluginActivator;
import systems.dmx.core.util.JavaUtils;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
// import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;



@Path("/deepl")
@Consumes("application/json")
@Produces("application/json")
public class DeepLPlugin extends PluginActivator implements DeepLService {

    private static final String DEEPL_URL = "https://api-free.deepl.com/v2/";
    private static final String DEEPL_AUTH_KEY = System.getProperty("dmx.deepl.auth_key");

    private static final String IMG_START = "<img src=\"";
    private static final String IMG_END = "\">";

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    // *** DeepLService ***

    @Override
    public List<Translation> translate(@QueryParam("text") String text, @QueryParam("target_lang") String targetLang) {
        try {
            StringBuilder stripped = new StringBuilder();
            List<String> urls = stripImageURLs(text, stripped);
            String _stripped = stripped.toString();
            logger.info("Translating text (image URLs stripped): \"" + _stripped + "\", targetLang=\"" + targetLang +
                "\"");
            URLConnection con = new URL(DEEPL_URL + "translate").openConnection();
            con.setRequestProperty("Authorization", "DeepL-Auth-Key " + DEEPL_AUTH_KEY);
            con.setDoOutput(true);
            // Note: opening the output stream connects implicitly (no con.connect() required)
            // and sets method to "POST" automatically
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write("text=" + _stripped + "&target_lang=" + targetLang + "&tag_handling=xml");
            out.flush();
            String responseData = JavaUtils.readText(con.getInputStream());
            logger.info("responseData=" + responseData);
            // parse response
            JSONArray translations = new JSONObject(responseData).getJSONArray("translations");
            List result = new ArrayList();
            for (int i = 0; i < translations.length(); i++) {
                JSONObject translation = translations.getJSONObject(i);
                result.add(new Translation(
                    insertImageURLs(translation.getString("text"), urls),
                    translation.getString("detected_source_language")
                ));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Translation failed", e);
        }
    }

    @GET
    @Path("/usage")
    @Override
    public InputStream usageStats() {
        try {
            URLConnection con = new URL(DEEPL_URL + "usage").openConnection();
            con.setRequestProperty("Authorization", "DeepL-Auth-Key " + DEEPL_AUTH_KEY);
            con.connect();
            return con.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Getting usage stats failed", e);
        }
    }

    // ------------------------------------------------------------------------------------------------- Private Methods

    private List<String> stripImageURLs(String text, StringBuilder builder) {
        List<String> urls = new ArrayList();
        int t1;
        int ti = 0;
        while ((t1 = text.indexOf(IMG_START, ti)) != -1) {
            int t2 = text.indexOf(IMG_END, ti + IMG_START.length());
            String url = text.substring(t1 + IMG_START.length(), t2);
            urls.add(url);
            builder.append(text.substring(ti, t1 + IMG_START.length()));
            builder.append(IMG_END);
            ti = t2 + IMG_END.length();
        }
        builder.append(text.substring(ti));
        return urls;
    }

    private String insertImageURLs(String text, List<String> urls) {
        StringBuilder inserted = new StringBuilder();
        int t1;
        int ti = 0;
        int count = 0;
        while ((t1 = text.indexOf(IMG_START, ti)) != -1) {
            inserted.append(text.substring(ti, t1 + IMG_START.length()));
            inserted.append(urls.get(count++));
            inserted.append(IMG_END);
            ti = t1 + IMG_START.length() + IMG_END.length();
        }
        inserted.append(text.substring(ti));
        return inserted.toString();
    }
}
