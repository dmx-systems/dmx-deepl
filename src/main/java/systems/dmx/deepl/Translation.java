package systems.dmx.deepl;

import systems.dmx.core.JSONEnabled;
import org.codehaus.jettison.json.JSONObject;



public class Translation implements JSONEnabled {

    public String text;
    public String detectedSourceLang;

    public Translation(String text, String detectedSourceLang) {
        this.text = text;
        this.detectedSourceLang = detectedSourceLang;
    }

    // JSONEnabled

    @Override
    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("text", text)
                .put("detectedSourceLang", detectedSourceLang);
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }
}
