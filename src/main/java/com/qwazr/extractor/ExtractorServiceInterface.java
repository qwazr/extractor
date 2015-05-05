/**
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.qwazr.utils.server.RestApplication;

@Path("/extractor")
public interface ExtractorServiceInterface {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ResourceLink> list();

	@GET
	@Path("/{name}")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Object get(@Context UriInfo uriInfo,
			@PathParam("name") String parserName,
			@QueryParam("path") String path);

	@PUT
	@Path("/{name}")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public ParserResult put(@Context UriInfo uriInfo,
			@PathParam("name") String parserName,
			@QueryParam("path") String filePath, InputStream inputStream);

	public ParserResult extract(String parserName,
			MultivaluedMap<String, String> parameters, String filePath,
			InputStream inputStream);

	@PUT
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public ParserResult putMagic(@Context UriInfo uriInfo,
			@QueryParam("name") String fileName,
			@QueryParam("path") String filePath,
			@QueryParam("type") String mimeType, InputStream inputStream);
}
