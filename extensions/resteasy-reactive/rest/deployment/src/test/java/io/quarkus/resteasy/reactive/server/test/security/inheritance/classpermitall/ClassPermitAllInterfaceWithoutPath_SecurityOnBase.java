package io.quarkus.resteasy.reactive.server.test.security.inheritance.classpermitall;

import static io.quarkus.resteasy.reactive.server.test.security.inheritance.SubPaths.CLASS_PATH_ON_RESOURCE;
import static io.quarkus.resteasy.reactive.server.test.security.inheritance.SubPaths.CLASS_PERMIT_ALL_METHOD_PERMIT_ALL_PATH;
import static io.quarkus.resteasy.reactive.server.test.security.inheritance.SubPaths.CLASS_PERMIT_ALL_PATH;
import static io.quarkus.resteasy.reactive.server.test.security.inheritance.SubPaths.IMPL_ON_BASE;
import static io.quarkus.resteasy.reactive.server.test.security.inheritance.SubPaths.INTERFACE_METHOD_WITH_PATH;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.vertx.core.json.JsonObject;

public interface ClassPermitAllInterfaceWithoutPath_SecurityOnBase {

    @POST
    @Path(CLASS_PATH_ON_RESOURCE + IMPL_ON_BASE + INTERFACE_METHOD_WITH_PATH + CLASS_PERMIT_ALL_PATH)
    String classPathOnResource_ImplOnBase_InterfaceMethodWithPath_ClassPermitAll(JsonObject array);

    @POST
    @Path(CLASS_PATH_ON_RESOURCE + IMPL_ON_BASE + INTERFACE_METHOD_WITH_PATH + CLASS_PERMIT_ALL_METHOD_PERMIT_ALL_PATH)
    String classPathOnResource_ImplOnBase_InterfaceMethodWithPath_ClassPermitAllMethodPermitAll(JsonObject array);

}
