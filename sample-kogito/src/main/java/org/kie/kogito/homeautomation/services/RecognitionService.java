package org.kie.kogito.homeautomation.services;

import io.vertx.core.json.JsonArray;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.homeautomation.ImageData;
import org.kie.kogito.homeautomation.util.PostData;
import org.kie.kogito.homeautomation.util.RestService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.kie.kogito.homeautomation.util.RestRequest.of;

@ApplicationScoped
public class RecognitionService extends AbstractWelcomeHomeService {

    private static final String CONTENT_TYPE = "image/jpeg";
    private static final String fileName = "tmp.jpg";
    private static final String name = "file";
    private static final String unknown = "unknown";

    @Inject
    protected RestService service;

    @ConfigProperty(name = "recognition.service.host", defaultValue = "localhost")
    protected String host;

    @ConfigProperty(name = "recognition.service.port", defaultValue = "8080")
    protected int port;

    @ConfigProperty(name = "recognition.service.ssl", defaultValue = "false")
    protected boolean ssl;

    @ConfigProperty(name = "recognition.service.endpoint")
    protected String endpoint;

    public void recognize(String id, ImageData imageData) {
        LOGGER.info("RecognitionService.recognize");
        var request = of(host, port, ssl, endpoint);
        var postData = PostData.of(name, fileName, imageData.getImage(), CONTENT_TYPE);
        service.POSTForm(request, postData, rawQuote -> {
            LOGGER.info("result " + rawQuote.bodyAsString());
            var matches = rawQuote.bodyAsJsonObject().getJsonArray("faces");
            var user = extractMatchName(matches);
            LOGGER.info("Recognized user: " + user);
            signalToProcess(id, "receive-user", user);
        });
    }

    protected String extractMatchName(JsonArray matches) {
        if(matches.size() == 0) {
            return unknown;
        }
        var matchedId = matches.getJsonObject(0).getString("id");
        return matchedId.substring(0, matchedId.lastIndexOf("_"));
    }
}