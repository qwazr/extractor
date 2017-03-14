/**
 * Copyright 2015-2017 Emmanuel Keller
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

import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

class UriInfoMock implements UriInfo {

	private final MultivaluedMap<String, String> parameters;

	UriInfoMock(String... keyValueParams) {
		if (keyValueParams == null) {
			parameters = null;
			return;
		}
		parameters = new MultivaluedHashMap<>();
		for (int i = 0; i < keyValueParams.length; i += 2)
			parameters.add(keyValueParams[i], keyValueParams[i + 1]);
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public String getPath(boolean decode) {
		return null;
	}

	@Override
	public List<PathSegment> getPathSegments() {
		return null;
	}

	@Override
	public List<PathSegment> getPathSegments(boolean decode) {
		return null;
	}

	@Override
	public URI getRequestUri() {
		return null;
	}

	@Override
	public UriBuilder getRequestUriBuilder() {
		return null;
	}

	@Override
	public URI getAbsolutePath() {
		return null;
	}

	@Override
	public UriBuilder getAbsolutePathBuilder() {
		return null;
	}

	@Override
	public URI getBaseUri() {
		return null;
	}

	@Override
	public UriBuilder getBaseUriBuilder() {
		return null;
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters() {
		return null;
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters(boolean decode) {
		return null;
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		return parameters;
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
		return null;
	}

	@Override
	public List<String> getMatchedURIs() {
		return null;
	}

	@Override
	public List<String> getMatchedURIs(boolean decode) {
		return null;
	}

	@Override
	public List<Object> getMatchedResources() {
		return null;
	}

	@Override
	public URI resolve(URI uri) {
		return null;
	}

	@Override
	public URI relativize(URI uri) {
		return null;
	}
}
