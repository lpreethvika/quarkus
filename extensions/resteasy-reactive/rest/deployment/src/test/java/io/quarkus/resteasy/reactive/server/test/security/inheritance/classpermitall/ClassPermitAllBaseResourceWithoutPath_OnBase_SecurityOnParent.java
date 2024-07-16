package io.quarkus.resteasy.reactive.server.test.security.inheritance.classpermitall;

import static io.quarkus.resteasy.reactive.server.test.security.inheritance.SubPaths.CLASS_PATH_ON_RESOURCE;
import static io.quarkus.resteasy.reactive.server.test.security.inheritance.SubPaths.CLASS_PERMIT_ALL_PREFIX;
import static io.quarkus.resteasy.reactive.server.test.security.inheritance.SubPaths.CLASS_SECURITY_ON_PARENT;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path(CLASS_PERMIT_ALL_PREFIX + CLASS_SECURITY_ON_PARENT + CLASS_PATH_ON_RESOURCE)
public class ClassPermitAllBaseResourceWithoutPath_OnBase_SecurityOnParent
        extends ClassPermitAllParentResourceWithoutPath_PathOnBase_SecurityOnParent {
}
