package io.quarkus.devui.runtime;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.jboss.logging.Logger;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class LocalHostOnlyFilter implements Handler<RoutingContext> {
    private static final Logger LOG = Logger.getLogger(LocalHostOnlyFilter.class);

    private static final String LOCAL_HOST = "localhost";
    private static final String LOCAL_HOST_IP = "127.0.0.1";

    private final List<String> hosts;

    public LocalHostOnlyFilter(List<String> hosts) {
        this.hosts = hosts;
    }

    @Override
    public void handle(RoutingContext event) {
        HttpServerResponse response = event.response();
        if (hostIsValid(event)) {
            event.next();
        } else {
            LOG.error("Dev UI: Only localhost is allowed");
            response.setStatusCode(403);
            response.setStatusMessage("Dev UI: Only localhost is allowed - Invalid host");
            response.end();
        }
    }

    private boolean hostIsValid(RoutingContext event) {
        try {
            URI uri = new URI(event.request().absoluteURI());
            URL url = uri.toURL();
            String host = url.getHost();

            if (host.equals(LOCAL_HOST) || host.equals(LOCAL_HOST_IP)) {
                return true;
            }
            return this.hosts != null && this.hosts.contains(host);
        } catch (MalformedURLException | URISyntaxException e) {
            LOG.error("Error while checking if Dev UI is localhost", e);
        }
        return false;
    }
}
