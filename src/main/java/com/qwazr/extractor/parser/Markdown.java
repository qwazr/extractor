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
package com.qwazr.extractor.parser;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserFieldsBuilder;
import com.qwazr.extractor.ParserResultBuilder;
import com.qwazr.utils.CharsetUtils;
import com.qwazr.utils.StringUtils;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.TextContentRenderer;

import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Markdown extends ParserAbstract {

	final static String[] DEFAULT_MIMETYPES = { "text/x-markdown", "text/markdown" };

	final static String[] DEFAULT_EXTENSIONS = { "md", "markdown" };

	final static ParserField CONTENT = ParserField.newString("content", "The content of the document");

	final static ParserField URL = ParserField.newString("url", "Detected URLs");

	final static ParserField LANG_DETECTION = ParserField.newString("lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS = { CONTENT, URL, LANG_DETECTION };

	@Override
	public ParserField[] getParameters() {
		return null;
	}

	@Override
	public ParserField[] getFields() {
		return FIELDS;
	}

	@Override
	public String[] getDefaultExtensions() {
		return DEFAULT_EXTENSIONS;
	}

	@Override
	public String[] getDefaultMimeTypes() {
		return DEFAULT_MIMETYPES;
	}

	@Override
	final public void parseContent(final MultivaluedMap<String, String> parameters, final InputStream inputStream,
			final String extension, final String mimeType, final ParserResultBuilder resultBuilder) throws Exception {
		final ParserFieldsBuilder result = resultBuilder.newDocument();
		final Parser parser = Parser.builder().build();
		final TextContentRenderer renderer =
				TextContentRenderer.builder().nodeRendererFactory(context -> new ExtractorNodeRenderer(result)).build();
		try (final InputStreamReader reader = new InputStreamReader(inputStream, CharsetUtils.CharsetUTF8)) {
			final String[] lines = StringUtils.splitLines(renderer.render(parser.parseReader(reader)));
			for (String line : lines)
				result.add(CONTENT, line);
		}
	}

	private final static Set<Class<? extends Node>> TYPES =
			new HashSet<>(Arrays.asList(Link.class, org.commonmark.node.Image.class));

	final public class ExtractorNodeRenderer implements NodeRenderer {

		private final ParserFieldsBuilder result;

		private ExtractorNodeRenderer(final ParserFieldsBuilder result) {
			this.result = result;
		}

		@Override
		public Set<Class<? extends Node>> getNodeTypes() {
			return TYPES;
		}

		@Override
		public void render(Node node) {
			if (node instanceof Link) {
				final Link link = (Link) node;
				result.add(URL, link.getDestination());
				result.add(CONTENT, link.getTitle());
			} else if (node instanceof org.commonmark.node.Image) {
				final org.commonmark.node.Image img = (org.commonmark.node.Image) node;
				result.add(URL, img.getDestination());
				result.add(CONTENT, img.getTitle());
			}
		}
	}
}
