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

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

public class UriInfoImplTest {

	private final UriInfo uriInfo;

	public UriInfoImplTest() throws URISyntaxException {
		uriInfo = new UriInfoImpl("http://localhost:9090/service/",
				"extractor/html?raw=valueA&raw=valueB&enc=valu%C3%A9");
	}

	@Test
	public void allTests() throws URISyntaxException {

		Assert.assertEquals(new URI("http://localhost:9090/service/extractor/html"), uriInfo.getAbsolutePath());
		Assert.assertEquals(new URI("http://localhost:9090/service/"), uriInfo.getBaseUri());
		Assert.assertEquals(
				new URI("http://localhost:9090/service/extractor/html?raw=valueA&raw=valueB&enc=valu%C3%A9"),
				uriInfo.getRequestUri());

		Assert.assertEquals(new URI("http://localhost:9090/service/extractor/html"),
				uriInfo.getAbsolutePathBuilder().build());
		Assert.assertEquals(new URI("http://localhost:9090/service/"), uriInfo.getBaseUriBuilder().build());
		Assert.assertEquals(
				new URI("http://localhost:9090/service/extractor/html?raw=valueA&raw=valueB&enc=valu%C3%A9"),
				uriInfo.getRequestUriBuilder().build());

		Assert.assertEquals("extractor/html", uriInfo.getPath());
		Assert.assertEquals("extractor/html", uriInfo.getPath(false));
		Assert.assertEquals("extractor/html", uriInfo.getPath(true));

		Assert.assertArrayEquals(new String[] { "valueA", "valueB" },
				uriInfo.getQueryParameters().get("raw").toArray());
		Assert.assertArrayEquals(new String[] { "valueA", "valueB" },
				uriInfo.getQueryParameters(false).get("raw").toArray());
		Assert.assertArrayEquals(new String[] { "valueA", "valueB" },
				uriInfo.getQueryParameters(true).get("raw").toArray());

		Assert.assertEquals("valué", uriInfo.getQueryParameters().getFirst("enc"));
		Assert.assertEquals("valué", uriInfo.getQueryParameters(true).getFirst("enc"));
		Assert.assertEquals("valu%C3%A9", uriInfo.getQueryParameters(false).getFirst("enc"));

		Assert.assertEquals("extractor", uriInfo.getPathSegments().get(0).getPath());
		Assert.assertEquals("extractor", uriInfo.getPathSegments(true).get(0).getPath());
		Assert.assertEquals("extractor", uriInfo.getPathSegments(false).get(0).getPath());

		Assert.assertEquals("html", uriInfo.getPathSegments().get(1).getPath());
		Assert.assertEquals("html", uriInfo.getPathSegments(true).get(1).getPath());
		Assert.assertEquals("html", uriInfo.getPathSegments(false).get(1).getPath());

		Assert.assertEquals(new URI("relative"),
				uriInfo.relativize(new URI("http://localhost:9090/service/extractor/html/relative")));
		Assert.assertEquals(new URI("http://localhost:9090/service/extractor/pdf"),
				uriInfo.resolve(new URI("extractor/pdf")));

		final UriInfo noPathNoQueyUri = new UriInfoImpl("http://localhost", "");
		Assert.assertEquals(0, noPathNoQueyUri.getQueryParameters().size());
		Assert.assertEquals(0, noPathNoQueyUri.getPathSegments().size());
	}

	@Test(expected = IllegalStateException.class)
	public void getPathParameters() {
		uriInfo.getPathParameters();
	}

	@Test(expected = NotImplementedException.class)
	public void getMatchedResources() {
		uriInfo.getMatchedResources();
	}

	@Test(expected = NotImplementedException.class)
	public void getMatchedURIs() {
		uriInfo.getMatchedURIs();
	}

	@Test(expected = NotImplementedException.class)
	public void getMatchedURIsDecode() {
		uriInfo.getMatchedURIs(true);
	}

	@Test(expected = NotImplementedException.class)
	public void getMatrixParameters() {
		uriInfo.getPathSegments().get(0).getMatrixParameters();
	}
}


