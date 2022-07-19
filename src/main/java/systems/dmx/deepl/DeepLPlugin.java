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
            logger.info("Translating text=\"" + text + "\", targetLang=\"" + targetLang + "\"");
            URLConnection con = new URL(DEEPL_URL + "translate").openConnection();
            con.setRequestProperty("Authorization", "DeepL-Auth-Key " + DEEPL_AUTH_KEY);
            con.setDoOutput(true);
            // Note: opening the output stream connects implicitly (no con.connect() required)
            // and sets method to "POST" automatically
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write("text=" + text + "&target_lang=" + targetLang + "&tag_handling=xml");
            out.flush();
            String responseData = JavaUtils.readText(con.getInputStream());
            logger.info("responseData=" + responseData);
            // parse response
            JSONArray translations = new JSONObject(responseData).getJSONArray("translations");
            List result = new ArrayList();
            for (int i = 0; i < translations.length(); i++) {
                JSONObject translation = translations.getJSONObject(i);
                result.add(new Translation(
                    repairTranslation(translation.getString("text"), text),
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
            con.setRequestProperty("Authorization", "DeepL-Auth-Key " + DEEPL_AUTH_KEY);
            con.connect();
            return con.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Getting usage stats failed", e);
        }
    }

    // ------------------------------------------------------------------------------------------------- Private Methods

    // Note: image tag data-urls are mangled by DeepL: '+' characters are (accidentally) stripped.
    private String repairTranslation(String translation, String text) {
        try {
            StringBuilder repaired = new StringBuilder();
            int ti = 0;
            int ri = 0;
            int t1;
            int count = 0;
            while ((t1 = text.indexOf(IMG_START, ti)) != -1) {
                int t2 = text.indexOf(IMG_END, ti + IMG_START.length());
                int r1 = translation.indexOf(IMG_START, ri);
                int r2 = translation.indexOf(IMG_END, ri + IMG_START.length());
                if (r1 == -1 || r2 == -1) {
                    throw new RuntimeException("not found in translation");
                }
                repaired.append(translation.substring(ri, r1));
                repaired.append(text.substring(t1, t2 + IMG_END.length()));
                ti = t2 + IMG_END.length();
                ri = r2 + IMG_END.length();
                count++;
            }
            repaired.append(translation.substring(ri));
            logger.info("Image URLs repaired: " + count);
            return repaired.toString();
        } catch (Exception e) {
            throw new RuntimeException("Repairing image tags failed", e);
        }
    }
}
