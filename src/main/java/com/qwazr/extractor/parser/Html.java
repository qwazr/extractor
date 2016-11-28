/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.XPathParser;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import se.fishtank.css.selectors.Selectors;
import se.fishtank.css.selectors.dom.W3CNode;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Html extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = { "text/html" };

	public static final String[] DEFAULT_EXTENSIONS = { "htm", "html" };

	final protected static ParserField TITLE = ParserField.newString("title", "The title of the document");

	final protected static ParserField CONTENT =
			ParserField.newString("content", "The text content of the document. One item per paragraph");

	final protected static ParserField H1 = ParserField.newString("h1", "H1 header contents");

	final protected static ParserField H2 = ParserField.newString("h2", "H2 header contents");

	final protected static ParserField H3 = ParserField.newString("h3", "H3 header contents");

	final protected static ParserField H4 = ParserField.newString("h4", "H4 header contents");

	final protected static ParserField H5 = ParserField.newString("h5", "H5 header contents");

	final protected static ParserField H6 = ParserField.newString("h6", "H6 header contents");

	final protected static ParserField ANCHORS = ParserField.newString("anchors", "Anchors");

	final protected static ParserField IMAGES = ParserField.newMap("images", "Image tags");

	final protected static ParserField METAS = ParserField.newMap("metas", "Meta tags");

	final protected static ParserField XPATH = ParserField.newMap("xpath", "XPath selector results");

	final protected static ParserField CSS = ParserField.newMap("css", "CSS selector results");

	final protected static ParserField LANG_DETECTION =
			ParserField.newString("lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS =
			{ TITLE, CONTENT, H1, H2, H3, H4, H5, H6, ANCHORS, IMAGES, METAS, LANG_DETECTION, XPATH };

	final protected static ParserField XPATH_PARAM = ParserField.newString("xpath", "Any XPATH selector");

	final protected static ParserField XPATH_NAME_PARAM =
			ParserField.newString("xpath_name", "The name of the XPATH selector");

	final protected static ParserField CSS_PARAM = ParserField.newString("css", "Any CSS selector");

	final protected static ParserField CSS_NAME_PARAM =
			ParserField.newString("css_name", "The name of the CSS selector");

	final protected static ParserField[] PARAMETERS = { XPATH_PARAM, XPATH_NAME_PARAM, CSS_PARAM, CSS_NAME_PARAM };

	@Override
	protected ParserField[] getParameters() {
		return PARAMETERS;
	}

	@Override
	protected ParserField[] getFields() {
		return FIELDS;
	}

	private void extractTitle(final XPathParser xpath, final Document documentElement, final ParserDocument document)
			throws XPathExpressionException {
		final String title = xpath.evaluateString(documentElement, "/html/head/title");
		if (title != null)
			document.add(TITLE, title);
	}

	private void extractHeaders(final Document documentElement, final ParserDocument document) {
		addToField(document, H1, documentElement.getElementsByTagName("h1"));
		addToField(document, H2, documentElement.getElementsByTagName("h2"));
		addToField(document, H3, documentElement.getElementsByTagName("h3"));
		addToField(document, H4, documentElement.getElementsByTagName("h4"));
		addToField(document, H5, documentElement.getElementsByTagName("h5"));
		addToField(document, H6, documentElement.getElementsByTagName("h6"));
	}

	private void extractAnchors(final XPathParser xpath, final Document documentElement, final ParserDocument document)
			throws XPathExpressionException {
		final XPathParser.NodeIterator iterator = xpath.evaluateNodes(documentElement, "//a/@href");
		while (iterator.hasNext())
			document.add(ANCHORS, iterator.next().getAttributes().getNamedItem("href").getTextContent());
	}

	private void extractImgTags(final Document documentElement, final ParserDocument document) {
		final XPathParser.NodeIterator iterator =
				new XPathParser.NodeIterator(documentElement.getElementsByTagName("img"));
		while (iterator.hasNext()) {
			final Node node = iterator.next();
			final Map<String, String> map = new LinkedHashMap<>();
			addToMap(map, "src", XPathParser.getAttributeString(node, "src"));
			addToMap(map, "alt", XPathParser.getAttributeString(node, "alt"));
			if (!map.isEmpty())
				document.add(IMAGES, map);
		}
	}

	private void extractTextContent(final Document documentElement, final ParserDocument document) throws IOException {
		String text = documentElement.asText();
		if (text == null)
			return;
		final ArrayList<String> lines = new ArrayList<>();
		StringUtils.linesCollector(text, false, lines);
		for (String line : lines) {
			line = line.trim();
			if (!StringUtils.isEmpty(line))
				document.add(CONTENT, line);
		}
		// Lang detection
		document.add(LANG_DETECTION, languageDetection(CONTENT, 10000));
	}

	private void extractMeta(final HtmlPage page, final ParserDocument document) {
		HtmlElement head = page.getHead();
		if (head == null)
			return;
		final List<HtmlElement> metas = head.getElementsByTagName("meta");
		if (metas != null) {
			final Map<String, String> map = new LinkedHashMap<>();
			for (DomElement meta : metas) {
				String name = meta.getAttribute("name");
				String content = meta.getAttribute("content");
				if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(content))
					map.put(name, content);
			}
			if (!map.isEmpty())
				document.add(METAS, map);
		}
	}

	private final List<String> dumpSelectors(final Object object) {
		final List<String> textList = new ArrayList<>();
		if (object == null)
			return textList;
		if (object instanceof NodeList) {
			final NodeList nodeList = (NodeList) object;
			int length = nodeList.getLength();
			for (int i = 0; i < length; i++)
				textList.add(nodeList.item(i).getTextContent());
		} else if (object instanceof Node) {
			final Node node = (Node) object;
			textList.add(node.getTextContent());
		}
		return textList;
	}

	private final List<Object> getXPath(final XPathParser xPath, final String query, final Document doc)
			throws XPathExpressionException {
		final List<Object> list = new ArrayList<>();
		xPath.evaluate(doc, query, new XPathParser.Consumer() {
			@Override
			public void accept(Node object) {
				list.add(object.getTextContent());
			}

			@Override
			public void accept(Boolean object) {
				list.add(object);
			}

			@Override
			public void accept(String object) {
				list.add(object);
			}

			@Override
			public void accept(Number object) {
				list.add(object);
			}
		});
		return list;
	}

	private final int extractXPath(final XPathParser xPath, final Document htmlDocument, final ParserDocument document)
			throws XPathExpressionException {
		int i = 0;
		String xpath;
		while ((xpath = getParameterValue(XPATH_PARAM, i)) != null) {
			final String name = getParameterValue(XPATH_NAME_PARAM, i);
			final LinkedHashMap<String, Object> xpathResult = new LinkedHashMap<>();
			xpathResult.put(name == null ? Integer.toString(i) : name,
					dumpSelectors(getXPath(xPath, xpath, htmlDocument)));
			document.add(XPATH, xpathResult);
			i++;
		}
		return i;
	}

	private int extractCss(final Document htmlDocument, final ParserDocument document) {
		int i = 0;
		String css;
		final Selectors selectors = new Selectors<>(new W3CNode(htmlDocument));
		while ((css = getParameterValue(CSS_PARAM, i)) != null) {
			final String name = getParameterValue(CSS_NAME_PARAM, i);
			final LinkedHashMap<String, Object> cssResult = new LinkedHashMap<>();
			final List<Node> results = selectors.querySelectorAll(css);
			cssResult.put(name == null ? Integer.toString(i) : name, dumpSelectors(results));
			document.add(CSS, cssResult);
			i++;
		}
		return i;
	}

	private void addToMap(final Map<String, String> map, final String name, final String value) {
		if (!StringUtils.isEmpty(value))
			map.put(name, value);
	}

	private void addToField(ParserDocument document, ParserField parserField, NodeList elements) {
		if (elements == null)
			return;
		final XPathParser.NodeIterator nodeIterator = new XPathParser.NodeIterator(elements);
		while (nodeIterator.hasNext())
			document.add(parserField, nodeIterator.next().getTextContent());
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception {
		final DOMParser htmlParser = new DOMParser();
		htmlParser.setFeature("http://xml.org/sax/features/namespaces", true);
		htmlParser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);
		htmlParser.setFeature("http://cyberneko.org/html/features/balance-tags", true);
		htmlParser.setFeature("http://cyberneko.org/html/features/report-errors", false);
		htmlParser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
		htmlParser.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower");
		htmlParser.parse(new InputSource(inputStream));

		final ParserDocument parserDocument = getNewParserDocument();
		final Document htmlDocument = htmlParser.getDocument();

		final XPathParser xPath = new XPathParser();

		if (extractXPath(xPath, htmlDocument, parserDocument) + extractCss(htmlDocument, parserDocument) == 0) {
			extractTitle(page, parserDocument);
			extractHeaders(documentElement, parserDocument);
			extractAnchors(page, parserDocument);
			extractImgTags(page, parserDocument);
			extractTextContent(page, parserDocument);
			extractMeta(page, parserDocument);
		}
	}

	@Override
	protected String[] getDefaultExtensions() {
		return DEFAULT_EXTENSIONS;
	}

	@Override
	protected String[] getDefaultMimeTypes() {
		return DEFAULT_MIMETYPES;
	}

}
