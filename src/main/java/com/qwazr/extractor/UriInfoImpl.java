/*
 * Copyright 2015-2018 Emmanuel Keller
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

import com.qwazr.utils.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UriInfoImpl implements UriInfo {

	private final URI baseUri;
	private final URI absoluteUri;
	private final URI relativeUri;

	private volatile URI absolutePath;
	private volatile List<PathSegment> rawPathSegments;
	private volatile List<PathSegment> decodedPathSegments;
	private volatile MultivaluedMap<String, String> rawParameters;
	private volatile MultivaluedMap<String, String> decodedParameters;

	public UriInfoImpl(final URI base, final URI child) {
		baseUri = base;
		absoluteUri = child.isAbsolute() ? child : base.resolve(child);
		relativeUri = base.relativize(child);
	}

	public UriInfoImpl(final String base, final String child) throws URISyntaxException {
		this(new URI(base), new URI(child));
	}

	@Override
	public String getPath() {
		return relativeUri.getPath();
	}

	@Override
	public String getPath(boolean decode) {
		return decode ? getPath() : relativeUri.getRawPath();
	}

	@Override
	public synchronized List<PathSegment> getPathSegments() {
		if (decodedPathSegments == null)
			decodedPathSegments = pathSegments(relativeUri.getPath());
		return decodedPathSegments;
	}

	@Override
	public synchronized List<PathSegment> getPathSegments(boolean decode) {
		if (decode)
			return getPathSegments();
		if (rawPathSegments == null)
			rawPathSegments = pathSegments(relativeUri.getRawPath());
		return rawPathSegments;
	}

	@Override
	public URI getRequestUri() {
		return absoluteUri;
	}

	@Override
	public UriBuilder getRequestUriBuilder() {
		return new JerseyUriBuilder().uri(absoluteUri);
	}

	@Override
	public synchronized URI getAbsolutePath() {
		if (absolutePath == null)
			absolutePath = baseUri.resolve(getPath(false));
		return absolutePath;
	}

	@Override
	public UriBuilder getAbsolutePathBuilder() {
		return new JerseyUriBuilder().uri(getAbsolutePath());
	}

	@Override
	public URI getBaseUri() {
		return baseUri;
	}

	@Override
	public UriBuilder getBaseUriBuilder() {
		return new JerseyUriBuilder().uri(baseUri);
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters() {
		return getPathParameters(true);
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters(boolean decode) {
		throw new IllegalStateException("Not implemented");
	}

	private final static MultivaluedMap<String, String> EMPTY =
			new ImmutableMultivaluedMap<>(new MultivaluedHashMap<>());

	private MultivaluedMap<String, String> queryParameters(final String query) {
		if (query == null)
			return EMPTY;
		final String[] queryParts = StringUtils.split(query, '&');
		final MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
		for (String queryPart : queryParts) {
			final String[] keyValue = StringUtils.split(queryPart, '=');
			final String key = keyValue[0];
			for (int i = 1; i < keyValue.length; i++)
				multivaluedMap.add(key, keyValue[i]);
		}
		return multivaluedMap;
	}

	@Override
	public synchronized MultivaluedMap<String, String> getQueryParameters() {
		if (decodedParameters == null)
			decodedParameters = queryParameters(relativeUri.getQuery());
		return decodedParameters;
	}

	@Override
	public synchronized MultivaluedMap<String, String> getQueryParameters(boolean decode) {
		if (decode)
			return getQueryParameters();
		if (rawParameters == null)
			rawParameters = queryParameters(relativeUri.getRawQuery());
		return rawParameters;
	}

	@Override
	public List<String> getMatchedURIs() {
		throw new NotImplementedException("Not implemented");
	}

	@Override
	public List<String> getMatchedURIs(boolean decode) {
		throw new NotImplementedException("Not implemented");
	}

	@Override
	public List<Object> getMatchedResources() {
		throw new NotImplementedException("Not implemented");
	}

	@Override
	public URI resolve(URI uri) {
		return baseUri.resolve(uri).normalize();
	}

	@Override
	public URI relativize(final URI uri) {
		return absolutePath.relativize(uri);
	}

	private List<PathSegment> pathSegments(final String path) {
		if (path == null || path.isEmpty())
			return Collections.emptyList();
		final String[] pathParts = StringUtils.split(path, '/');
		final List<PathSegment> pathSegments = new ArrayList<>(pathParts.length);
		for (String pathPart : pathParts)
			pathSegments.add(new PathSegmentImpl(pathPart));
		return pathSegments;
	}

	private final class PathSegmentImpl implements PathSegment {

		private final String path;

		private PathSegmentImpl(final String path) {
			this.path = path;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public MultivaluedMap<String, String> getMatrixParameters() {
			throw new NotImplementedException("Not implemented");
		}
	}
}
