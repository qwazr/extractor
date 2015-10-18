/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor;

import com.qwazr.utils.server.RestApplication;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.util.Map;

@RolesAllowed(ExtractorServer.SERVICE_NAME_EXTRACTOR)
@Path("/extractor")
public interface ExtractorServiceInterface {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ResourceLink> list();

	@GET
	@Path("/{name}")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Object get(@Context UriInfo uriInfo, @PathParam("name") String parserName, @QueryParam("path") String path);

	@PUT
	@Path("/{name}")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public ParserResult put(@Context UriInfo uriInfo, @PathParam("name") String parserName,
					@QueryParam("path") String filePath, InputStream inputStream);

	public ParserResult extract(String parserName, MultivaluedMap<String, String> parameters, String filePath,
					InputStream inputStream);

	@PUT
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public ParserResult putMagic(@Context UriInfo uriInfo, @QueryParam("name") String fileName,
					@QueryParam("path") String filePath, @QueryParam("type") String mimeType, InputStream inputStream);
}
