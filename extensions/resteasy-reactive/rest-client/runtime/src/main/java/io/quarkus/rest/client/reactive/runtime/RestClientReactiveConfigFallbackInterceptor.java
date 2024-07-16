package io.quarkus.rest.client.reactive.runtime;

import java.util.function.Function;

import io.quarkus.runtime.util.StringUtil;
import io.smallrye.config.FallbackConfigSourceInterceptor;

/**
 * @deprecated maps the old config to the new config, should be removed at some point
 */
@Deprecated(forRemoval = true, since = "3.9")
public class RestClientReactiveConfigFallbackInterceptor extends FallbackConfigSourceInterceptor {

    private static final String OLD_PREFIX = "quarkus.rest-client-reactive.";
    private static final String NEW_PREFIX = "quarkus.rest-client.";
    private static final Function<String, String> RENAME_FUNCTION = new Function<String, String>() {
        @Override
        public String apply(String s) {
            return StringUtil.changePrefix(s, NEW_PREFIX, OLD_PREFIX);
        }
    };

    public RestClientReactiveConfigFallbackInterceptor() {
        super(RENAME_FUNCTION);
    }
}
