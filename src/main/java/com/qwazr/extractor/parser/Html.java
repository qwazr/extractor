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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserFieldsBuilder;
import com.qwazr.extractor.ParserResultBuilder;
import com.qwazr.utils.DomUtils;
import com.qwazr.utils.HtmlUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.XPathParser;
import org.apache.xerces.parsers.DOMParser;
import org.cyberneko.html.HTMLConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import se.fishtank.css.selectors.Selectors;
import se.fishtank.css.selectors.dom.W3CNode;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Html extends ParserAbstract {

	final private static String[] DEFAULT_MIMETYPES = { "text/html" };

	final private static String[] DEFAULT_EXTENSIONS = { "htm", "html" };

	final private static ParserField TITLE = ParserField.newString("title", "The title of the document");

	final private static ParserField CONTENT =
			ParserField.newString("content", "The text content of the document. One item per paragraph");

	final private static ParserField H1 = ParserField.newString("h1", "H1 header contents");

	final private static ParserField H2 = ParserField.newString("h2", "H2 header contents");

	final private static ParserField H3 = ParserField.newString("h3", "H3 header contents");

	final private static ParserField H4 = ParserField.newString("h4", "H4 header contents");

	final private static ParserField H5 = ParserField.newString("h5", "H5 header contents");

	final private static ParserField H6 = ParserField.newString("h6", "H6 header contents");

	final private static ParserField ANCHORS = ParserField.newString("anchors", "Anchors");

	final private static ParserField IMAGES = ParserField.newMap("images", "Image tags");

	final private static ParserField METAS = ParserField.newMap("metas", "Meta tags");

	final private static ParserField SELECTORS = ParserField.newMap("selectors", "Selector results");

	final private static ParserField LANG_DETECTION =
			ParserField.newString("lang_detection", "Detection of the language");

	final private static ParserField[] FIELDS =
			{ TITLE, CONTENT, H1, H2, H3, H4, H5, H6, ANCHORS, IMAGES, METAS, LANG_DETECTION, SELECTORS };

	final private static ParserField XPATH_PARAM = ParserField.newString("xpath", "Any XPATH selector");

	final private static ParserField XPATH_NAME_PARAM =
			ParserField.newString("xpath_name", "The name of the XPATH selector");

	final private static ParserField CSS_PARAM = ParserField.newString("css", "Any CSS selector");

	final private static ParserField CSS_NAME_PARAM = ParserField.newString("css_name", "The name of the CSS selector");

	final private static ParserField[] PARAMETERS = { XPATH_PARAM, XPATH_NAME_PARAM, CSS_PARAM, CSS_NAME_PARAM };

	@Override
	public ParserField[] getParameters() {
		return PARAMETERS;
	}

	@Override
	public ParserField[] getFields() {
		return FIELDS;
	}

	private void extractTitle(final XPathParser xpath, final Document documentElement,
			final ParserFieldsBuilder document) throws XPathExpressionException {
		final String title = xpath.evaluateString(documentElement, "/html/head/title");
		if (title != null)
			document.set(TITLE, title);
	}

	private void extractHeaders(final Document documentElement, final ParserFieldsBuilder document) {
		addToField(document, H1, documentElement.getElementsByTagName("h1"));
		addToField(document, H2, documentElement.getElementsByTagName("h2"));
		addToField(document, H3, documentElement.getElementsByTagName("h3"));
		addToField(document, H4, documentElement.getElementsByTagName("h4"));
		addToField(document, H5, documentElement.getElementsByTagName("h5"));
		addToField(document, H6, documentElement.getElementsByTagName("h6"));
	}

	private void extractAnchors(final XPathParser xpath, final Document documentElement,
			final ParserFieldsBuilder document) throws XPathExpressionException {
		xpath.evaluateNodes(documentElement, "//a/@href")
				.forEach(node -> document.add(ANCHORS, DomUtils.getAttributeString(node, "href")));
	}

	private void extractImgTags(final Document documentElement, final ParserFieldsBuilder document) {
		DomUtils.iterator(documentElement.getElementsByTagName("img")).forEach(node -> {
			final Map<String, String> map = new LinkedHashMap<>();
			addToMap(map, "src", DomUtils.getAttributeString(node, "src"));
			addToMap(map, "alt", DomUtils.getAttributeString(node, "alt"));
			if (!map.isEmpty())
				document.add(IMAGES, map);
		});
	}

	private void extractTextContent(final Document documentElement, final ParserFieldsBuilder document)
			throws IOException {
		HtmlUtils.domTextExtractor(documentElement, line -> document.add(CONTENT, line));
		// Lang detection
		document.add(LANG_DETECTION, languageDetection(document, CONTENT, 10000));
	}

	private void extractMeta(final Document documentElement, final ParserFieldsBuilder document) {
		NodeList nodeList = documentElement.getElementsByTagName("head");
		if (nodeList == null || nodeList.getLength() == 0)
			return;
		final Node head = nodeList.item(0);
		if (head.getNodeType() != Node.ELEMENT_NODE)
			return;
		final Map<String, String> map = new LinkedHashMap<>();
		DomUtils.iterator(((Element) head).getElementsByTagName("meta")).forEach(meta -> {
			final String name = DomUtils.getAttributeString(meta, "name");
			final String content = DomUtils.getAttributeString(meta, "content");
			if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(content))
				map.put(name, content);
		});
		if (!map.isEmpty())
			document.add(METAS, map);
	}

	private class ListConsumer extends ArrayList<Object> implements XPathParser.Consumer {

		@Override
		@JsonIgnore
		public void accept(Node object) {
			accept(object.getTextContent());
		}

		@Override
		@JsonIgnore
		public void accept(Boolean object) {
			add(object);
		}

		@Override
		@JsonIgnore
		public void accept(String object) {
			if (object != null)
				add(object.trim());
		}

		@Override
		@JsonIgnore
		public void accept(Number object) {
			add(object);
		}

	}

	private int extractXPath(final MultivaluedMap<String, String> parameters, final XPathParser xPath,
			final Node htmlDocument, final LinkedHashMap<String, Object> selectorsResult)
			throws XPathExpressionException {
		int i = 0;
		String xpath;
		while ((xpath = getParameterValue(parameters, XPATH_PARAM, i)) != null) {
			final String name = getParameterValue(parameters, XPATH_NAME_PARAM, i);
			final ListConsumer results = new ListConsumer();
			xPath.evaluate(htmlDocument, xpath, results);
			selectorsResult.put(name == null ? Integer.toString(i) : name, results);
			i++;
		}
		return i;
	}

	private int extractCss(final MultivaluedMap<String, String> parameters, final Node htmlDocument,
			final LinkedHashMap<String, Object> selectorsResult) {
		int i = 0;
		String css;
		final Selectors<Node, W3CNode> selectors = new Selectors<>(new W3CNode(htmlDocument));
		while ((css = getParameterValue(parameters, CSS_PARAM, i)) != null) {
			final String name = getParameterValue(parameters, CSS_NAME_PARAM, i);
			final ListConsumer results = new ListConsumer();
			selectors.querySelectorAll(css).forEach(results::accept);
			selectorsResult.put(name == null ? Integer.toString(i) : name, results);
			i++;
		}
		return i;
	}

	private void addToMap(final Map<String, String> map, final String name, final String value) {
		if (!StringUtils.isEmpty(value))
			map.put(name, value);
	}

	private void addToField(final ParserFieldsBuilder document, final ParserField parserField,
			final NodeList elements) {
		DomUtils.iterator(elements).forEach(node -> document.add(parserField, node.getTextContent()));
	}

	@Override
	public void parseContent(final MultivaluedMap<String, String> parameters, final InputStream inputStream,
			final String extension, final String mimeType, final ParserResultBuilder resultBuilder) throws Exception {
		final HTMLConfiguration config = new HTMLConfiguration();
		config.setFeature("http://xml.org/sax/features/namespaces", true);
		config.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);
		config.setFeature("http://cyberneko.org/html/features/balance-tags", true);
		config.setFeature("http://cyberneko.org/html/features/report-errors", false);
		config.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
		config.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower");
		final DOMParser htmlParser = new DOMParser(config);
		htmlParser.parse(new InputSource(inputStream));

		final ParserFieldsBuilder parserDocument = resultBuilder.newDocument();
		final Document htmlDocument = htmlParser.getDocument();

		final XPathParser xPath = new XPathParser();

		final LinkedHashMap<String, Object> selectorsResult = new LinkedHashMap<>();
		extractXPath(parameters, xPath, htmlDocument, selectorsResult);
		extractCss(parameters, htmlDocument, selectorsResult);
		if (!selectorsResult.isEmpty()) {
			parserDocument.set(SELECTORS, selectorsResult);
		} else {
			extractTitle(xPath, htmlDocument, parserDocument);
			extractHeaders(htmlDocument, parserDocument);
			extractAnchors(xPath, htmlDocument, parserDocument);
			extractImgTags(htmlDocument, parserDocument);
			extractTextContent(htmlDocument, parserDocument);
			extractMeta(htmlDocument, parserDocument);
		}
	}

	@Override
	public String[] getDefaultExtensions() {
		return DEFAULT_EXTENSIONS;
	}

	@Override
	public String[] getDefaultMimeTypes() {
		return DEFAULT_MIMETYPES;
	}

}
