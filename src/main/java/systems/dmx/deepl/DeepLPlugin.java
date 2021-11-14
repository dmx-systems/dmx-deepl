package systems.dmx.deepl;

import systems.dmx.core.osgi.PluginActivator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

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

    @GET
    //@Path("/comment/{targetTopicId}")
    //@Override
    public void method() {
        try {
        } catch (Exception e) {
            throw new RuntimeException("Method failed", e);
        }
    }
}
