package systems.dmx.deepl;

import java.io.InputStream;
import java.util.List;



public interface DeepLService {

    List<Translation> translate(String text, String targetLang);

    InputStream usageStats();
}
