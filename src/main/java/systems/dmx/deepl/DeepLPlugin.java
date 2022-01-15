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

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    // *** DeepLService ***

    @Override
    public List<Translation> translate(@QueryParam("text") String text, @QueryParam("target_lang") String targetLang) {
        try {
            logger.info("Translating text=\"" + text + "\", targetLang=\"" + targetLang + "\"");
            URLConnection con = new URL(DEEPL_URL + "translate").openConnection();
            con.setRequestProperty("Authorization", "DeepL-Auth-Key " + DEEPL_AUTH_KEY);
            con.setDoOutput(true);
            // Note: opening the output stream connects implicitly (no con.connect() required)
            // and sets method to "POST" automatically
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write("text=" + text + "&target_lang=" + targetLang + "&tag_handling=xml");
            out.flush();    // FIXME: URLEncoder.encode(text, "UTF-8")?
            String responseData = JavaUtils.readText(con.getInputStream());
            logger.info("responseData=" + responseData);
            // parse response
            JSONArray translations = new JSONObject(responseData).getJSONArray("translations");
            List result = new ArrayList();
            for (int i = 0; i < translations.length(); i++) {
                JSONObject translation = translations.getJSONObject(i);
                result.add(new Translation(
                    translation.getString("text"),
                    translation.getString("detected_source_language")
                ));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Translation failed, text=\"" + text + "\", targetLang=\"" + targetLang + "\"",
                e);
        }
    }

    @GET
    @Path("/usage")
    @Override
    public InputStream usageStats() {
        try {
            URLConnection con = new URL(DEEPL_URL + "usage").openConnection();
            con.addRequestProperty("Authorization", "DeepL-Auth-Key " + DEEPL_AUTH_KEY);
            con.connect();
            return con.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Getting usage stats failed", e);
        }
    }
}
