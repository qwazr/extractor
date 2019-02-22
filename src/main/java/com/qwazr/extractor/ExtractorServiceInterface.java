/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.util.Set;

@RolesAllowed(ExtractorServiceInterface.SERVICE_NAME)
@Path("/" + ExtractorServiceInterface.SERVICE_NAME)
public interface ExtractorServiceInterface extends ServiceInterface {

    String SERVICE_NAME = "extractor";

    @GET
    @Path("/")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    Set<String> list();

    @GET
    @Path("/{name}")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    Object get(@Context UriInfo uriInfo,
               @PathParam("name") String parserName,
               @QueryParam("path") String path);

    @PUT
    @Path("/{name}")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    ParserResult put(@Context UriInfo uriInfo,
                     @PathParam("name") String parserName,
                     @QueryParam("path") String filePath,
                     InputStream inputStream);

    ParserResult extract(String parserName,
                         MultivaluedMap<String, String> parameters,
                         String filePath,
                         InputStream inputStream);

    @PUT
    @Path("/")
    @Produces(ServiceInterface.APPLICATION_JSON_UTF8)
    ParserResult putMagic(@Context UriInfo uriInfo,
                          @QueryParam("name") String fileName,
                          @QueryParam("path") String filePath,
                          @QueryParam("type") String mimeType,
                          InputStream inputStream);

    ParserResult extractMagic(MultivaluedMap<String, String> parameters,
                              String fileName,
                              String filePath,
                              String mimeType,
                              InputStream inputStream);

}
