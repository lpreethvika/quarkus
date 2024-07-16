package io.quarkus.resteasy.reactive.server.test;

import static io.restassured.RestAssured.when;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

public class CancelableUniTest {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withApplicationRoot(jar -> jar.addClasses(Resource.class));

    @BeforeEach
    void setUp() {
        Resource.COUNT.set(0);
    }

    @Inject
    Vertx vertx;

    @TestHTTPResource
    URL url;

    @Test
    public void testNormal() {
        when().get("test")
                .then()
                .statusCode(200)
                .body(equalTo("Hello, world"));
    }

    @Test
    public void testCancel() {
        WebClient client = WebClient.create(vertx);

        client.get(url.getPort(), url.getHost(), "/test").send();

        try {
            // make sure we did make the proper request
            await().atMost(Duration.ofSeconds(2)).untilAtomic(Resource.COUNT, equalTo(1));

            // this will effectively cancel the request
            client.close();

            // make sure we wait until the request could have completed
            Thread.sleep(7_000);

            // if the count did not increase, it means that Uni was cancelled
            assertEquals(1, Resource.COUNT.get());
        } catch (InterruptedException ignored) {

        } finally {
            try {
                client.close();
            } catch (Exception ignored) {

            }
        }

    }

    @Path("test")
    public static class Resource {

        public static final AtomicInteger COUNT = new AtomicInteger(0);

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Uni<String> hello() {
            COUNT.incrementAndGet();
            return Uni.createFrom().item("Hello, world").onItem().delayIt().by(Duration.ofSeconds(5)).onItem().invoke(
                    COUNT::incrementAndGet);
        }
    }
}
