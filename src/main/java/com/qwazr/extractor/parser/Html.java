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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.*;
import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;
import com.qwazr.utils.StringUtils;

import java.io.File;
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

	final protected static ParserField CSS_PARAM = ParserField.newString("css", "Any CSS selector");

	final protected static ParserField[] PARAMETERS = { XPATH_PARAM, CSS_PARAM };

	@Override
	protected ParserField[] getParameters() {
		return PARAMETERS;
	}

	@Override
	protected ParserField[] getFields() {
		return FIELDS;
	}

	private void extractTitle(final HtmlPage page, final ParserDocument document) {
		String title = page.getTitleText();
		if (title != null)
			document.add(TITLE, title);
	}

	private void extractHeaders(final HtmlElement documentElement, final ParserDocument document) {
		addToField(document, H1, documentElement.getElementsByTagName("h1"));
		addToField(document, H2, documentElement.getElementsByTagName("h2"));
		addToField(document, H3, documentElement.getElementsByTagName("h3"));
		addToField(document, H4, documentElement.getElementsByTagName("h4"));
		addToField(document, H5, documentElement.getElementsByTagName("h5"));
		addToField(document, H6, documentElement.getElementsByTagName("h6"));
	}

	private void extractAnchors(final HtmlPage page, final ParserDocument document) {
		List<HtmlAnchor> anchors = page.getAnchors();
		if (anchors == null)
			return;
		for (HtmlAnchor anchor : anchors)
			document.add(ANCHORS, anchor.getHrefAttribute());
	}

	private void extractImgTags(final HtmlPage page, final ParserDocument document) {
		List<DomElement> elements = page.getElementsByTagName("img");
		if (elements == null)
			return;
		for (DomElement element : elements) {
			Map<String, String> map = new LinkedHashMap<>();
			addToMap(map, "src", element.getAttribute("src"));
			addToMap(map, "alt", element.getAttribute("alt"));
			if (!map.isEmpty())
				document.add(IMAGES, map);
		}
	}

	private void extractTextContent(final HtmlPage page, final ParserDocument document) throws IOException {
		String text = page.asText();
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

	private final List<String> dumpSelectors(final List<?> results) {
		final List<String> textList = new ArrayList<>();
		if (results == null)
			return textList;
		for (Object result : results) {
			final String content;
			if (result instanceof HtmlElement)
				content = ((HtmlElement) result).asText();
			else
				content = result.toString();
			textList.add(content);
		}
		return textList;
	}

	private final int extractXPath(final HtmlPage page, final ParserDocument document) {
		int i = 0;
		String xpath;
		while ((xpath = getParameterValue(XPATH, i)) != null) {
			final LinkedHashMap<String, Object> xpathResult = new LinkedHashMap<>();
			try {
				final List<?> results = page.getByXPath(xpath);
				xpathResult.put("text", dumpSelectors(results));
			} catch (Exception e) {
				xpathResult.put("error", e.getMessage());
			}
			document.add(XPATH, xpathResult);
			i++;
		}
		return i;
	}

	private final int extractCss(final HtmlPage page, final ParserDocument document) {
		int i = 0;
		String css;
		while ((css = getParameterValue(CSS, i)) != null) {
			final LinkedHashMap<String, Object> xpathResult = new LinkedHashMap<>();
			try {
				final DomNodeList<DomNode> results = page.querySelectorAll(css);
				xpathResult.put("text", dumpSelectors(results));
			} catch (Exception e) {
				xpathResult.put("error", e.getMessage());
			}
			document.add(CSS, xpathResult);
			i++;
		}
		return i;
	}

	@Override
	protected void parseContent(File file, String extension, String mimeType) throws Exception {

		try (WebClient webClient = new WebClient()) {

			final WebClientOptions options = webClient.getOptions();
			options.setJavaScriptEnabled(false);
			options.setCssEnabled(false);
			options.setThrowExceptionOnFailingStatusCode(false);
			options.setThrowExceptionOnScriptError(false);

			final Page unknownPage = webClient.getPage(file.toURI().toURL());
			if (!unknownPage.isHtmlPage())
				return;

			final HtmlPage page = (HtmlPage) unknownPage;
			final ParserDocument document = getNewParserDocument();
			final HtmlElement documentElement = page.getDocumentElement();

			if (extractXPath(page, document) == 0 && extractCss(page, document) == 0) {
				extractTitle(page, document);
				extractHeaders(documentElement, document);
				extractAnchors(page, document);
				extractImgTags(page, document);
				extractTextContent(page, document);
				extractMeta(page, document);
			}
		}
	}

	private void addToMap(Map<String, String> map, String name, String value) {
		if (!StringUtils.isEmpty(value))
			map.put(name, value);
	}

	private void addToField(ParserDocument document, ParserField parserField, DomNodeList<HtmlElement> elements) {
		if (elements == null)
			return;
		for (HtmlElement element : elements)
			document.add(parserField, element.asText());
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception {
		File tempFile = ParserAbstract.createTempFile(inputStream, extension == null ? "page.html" : "." + extension);
		try {
			parseContent(tempFile, extension, mimeType);
		} finally {
			tempFile.delete();
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
