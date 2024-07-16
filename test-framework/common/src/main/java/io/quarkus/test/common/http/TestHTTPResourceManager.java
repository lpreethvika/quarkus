package io.quarkus.test.common.http;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.bootstrap.app.RunningQuarkusApplication;
import io.quarkus.runtime.test.TestHttpEndpointProvider;

public class TestHTTPResourceManager {

    public static String getUri() {
        try {
            return sanitizeUri(ConfigProvider.getConfig().getValue("test.url", String.class));
        } catch (IllegalStateException e) {
            //massive hack for dev mode tests, dev mode has not started yet
            //so we don't have any way to load this correctly from config
            return "http://localhost:8080";
        }
    }

    public static String getManagementUri() {
        try {
            return sanitizeUri(ConfigProvider.getConfig().getValue("test.management.url", String.class));
        } catch (IllegalStateException e) {
            //massive hack for dev mode tests, dev mode has not started yet
            //so we don't have any way to load this correctly from config
            return "http://localhost:9000";
        }
    }

    public static String getSslUri() {
        return sanitizeUri(ConfigProvider.getConfig().getValue("test.url.ssl", String.class));
    }

    public static String getManagementSslUri() {
        return sanitizeUri(ConfigProvider.getConfig().getValue("test.management.url.ssl", String.class));
    }

    private static String sanitizeUri(String result) {
        if ((result != null) && result.endsWith("/")) {
            return result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String getUri(RunningQuarkusApplication application) {
        return sanitizeUri(application.getConfigValue("test.url", String.class).get());
    }

    public static String getSslUri(RunningQuarkusApplication application) {
        return sanitizeUri(application.getConfigValue("test.url.ssl", String.class).get());
    }

    public static void inject(Object testCase) {
        inject(testCase, TestHttpEndpointProvider.load());
    }

    public static void inject(Object testCase, List<Function<Class<?>, String>> endpointProviders) {
        Map<Class<?>, TestHTTPResourceProvider<?>> providers = getProviders();
        Class<?> c = testCase.getClass();
        while (c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                TestHTTPResource resource = f.getAnnotation(TestHTTPResource.class);
                if (resource != null) {
                    TestHTTPResourceProvider<?> provider = providers.get(f.getType());
                    if (provider == null) {
                        throw new RuntimeException(
                                "Unable to inject TestHTTPResource field " + f + " as no provider exists for the type");
                    }
                    String path = resource.value();
                    String endpointPath = null;
                    boolean management = resource.management();
                    TestHTTPEndpoint endpointAnnotation = f.getAnnotation(TestHTTPEndpoint.class);
                    if (endpointAnnotation != null) {
                        for (Function<Class<?>, String> func : endpointProviders) {
                            endpointPath = func.apply(endpointAnnotation.value());
                            if (endpointPath != null) {
                                break;
                            }
                        }
                        if (endpointPath == null) {
                            throw new RuntimeException(
                                    "Could not determine the endpoint path for " + endpointAnnotation.value() + " to inject "
                                            + f);
                        }
                    }
                    if (!path.isEmpty() && endpointPath != null) {
                        if (!endpointPath.endsWith("/")) {
                            path = endpointPath + "/" + path;
                        } else {
                            path = endpointPath + path;
                        }
                    } else if (endpointPath != null) {
                        path = endpointPath;
                    }
                    String val;
                    if (resource.ssl() || resource.tls()) {
                        if (management) {
                            if (path.startsWith("/")) {
                                val = getManagementSslUri() + path;
                            } else {
                                val = getManagementSslUri() + "/" + path;
                            }
                        } else {
                            if (path.startsWith("/")) {
                                val = getSslUri() + path;
                            } else {
                                val = getSslUri() + "/" + path;
                            }
                        }
                    } else {
                        if (management) {
                            if (path.startsWith("/")) {
                                val = getManagementUri() + path;
                            } else {
                                val = getManagementUri() + "/" + path;
                            }
                        } else {
                            if (path.startsWith("/")) {
                                val = getUri() + path;
                            } else {
                                val = getUri() + "/" + path;
                            }
                        }
                    }
                    f.setAccessible(true);
                    try {
                        f.set(testCase, provider.provide(val, f));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            c = c.getSuperclass();
        }
    }

    private static Map<Class<?>, TestHTTPResourceProvider<?>> getProviders() {
        Map<Class<?>, TestHTTPResourceProvider<?>> map = new HashMap<>();
        for (TestHTTPResourceProvider<?> i : ServiceLoader.load(TestHTTPResourceProvider.class,
                TestHTTPResourceProvider.class.getClassLoader())) {
            map.put(i.getProvidedType(), i);
        }
        return Collections.unmodifiableMap(map);
    }
}
