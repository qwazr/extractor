/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor;

import com.qwazr.server.ServiceInterface;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

@RolesAllowed(ExtractorServiceInterface.SERVICE_NAME)
@Path("/" + ExtractorServiceInterface.SERVICE_NAME)
public interface ExtractorServiceInterface extends ServiceInterface, ParserInterface {

    String SERVICE_NAME = "extractor";

    @GET
    @Path("/")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    Set<String> getParserNames();

    @GET
    @Path("/{name}")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    ParserDefinition getParserDefinition(final @PathParam("name") String parserName);

    @POST
    @Path("/")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    ParserResult extractFile(final @Context UriInfo uriInfo,
                             final String filePath);

    @PUT
    @Path("/")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    ParserResult extractStream(final @Context UriInfo uriInfo,
                               final @Context HttpHeaders headers,
                               final InputStream inputStream);


}
