/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.*;
import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;
import com.qwazr.utils.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Html extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = {"text/html"};

	public static final String[] DEFAULT_EXTENSIONS = {"htm", "html"};

	final protected static ParserField TITLE = ParserField.newString("title",
			"The title of the document");

	final protected static ParserField CONTENT = ParserField.newString(
			"content",
			"The text content of the document. One item per paragraph");

	final protected static ParserField H1 = ParserField.newString("h1",
			"H1 header contents");

	final protected static ParserField H2 = ParserField.newString("h2",
			"H2 header contents");

	final protected static ParserField H3 = ParserField.newString("h3",
			"H3 header contents");

	final protected static ParserField H4 = ParserField.newString("h4",
			"H4 header contents");

	final protected static ParserField H5 = ParserField.newString("h5",
			"H5 header contents");

	final protected static ParserField H6 = ParserField.newString("h6",
			"H6 header contents");

	final protected static ParserField ANCHORS = ParserField.newString(
			"anchors", "Anchors");

	final protected static ParserField IMAGES = ParserField.newMap("images",
			"Image tags");

	final protected static ParserField METAS = ParserField.newMap("metas",
			"Meta tags");

	final protected static ParserField LANG_DETECTION = ParserField.newString(
			"lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS = {TITLE, CONTENT, H1, H2, H3,
			H4, H5, H6, ANCHORS, IMAGES, METAS, LANG_DETECTION};

	final protected static ParserField[] PARAMETERS = {};

	@Override
	protected ParserField[] getParameters() {
		return PARAMETERS;
	}

	@Override
	protected ParserField[] getFields() {
		return FIELDS;
	}

	@Override
	protected void parseContent(File file, String extension, String mimeType)
			throws Exception {
		WebClient webClient = new WebClient();
		try {
			WebClientOptions options = webClient.getOptions();
			options.setJavaScriptEnabled(false);
			options.setCssEnabled(false);
			options.setThrowExceptionOnFailingStatusCode(false);
			options.setThrowExceptionOnScriptError(false);

			HtmlPage page = webClient.getPage(file.toURI().toURL());
			ParserDocument document = getNewParserDocument();

			String title = page.getTitleText();
			if (title != null)
				document.add(TITLE, title);

			addToField(document, H1, page.getDocumentElement()
					.getElementsByTagName("h1"));
			addToField(document, H2, page.getDocumentElement()
					.getElementsByTagName("h2"));
			addToField(document, H3, page.getDocumentElement()
					.getElementsByTagName("h3"));
			addToField(document, H4, page.getDocumentElement()
					.getElementsByTagName("h4"));
			addToField(document, H5, page.getDocumentElement()
					.getElementsByTagName("h5"));
			addToField(document, H6, page.getDocumentElement()
					.getElementsByTagName("h6"));

			List<HtmlAnchor> anchors = page.getAnchors();
			if (anchors != null)
				for (HtmlAnchor anchor : anchors)
					document.add(ANCHORS, anchor.getHrefAttribute());

			// Extract the IMG tags
			List<DomElement> elements = page.getElementsByTagName("img");
			if (elements != null) {
				for (DomElement element : elements) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					addToMap(map, "src", element.getAttribute("src"));
					addToMap(map, "alt", element.getAttribute("alt"));
					if (!map.isEmpty())
						document.add(IMAGES, map);
				}
			}

			// Get the text content
			String text = page.asText();
			if (text != null) {
				ArrayList<String> lines = new ArrayList<String>();
				StringUtils.linesCollector(text, false, lines);
				for (String line : lines) {
					line = line.trim();
					if (!StringUtils.isEmpty(line))
						document.add(CONTENT, line);
				}
			}

			// Lang detection
			document.add(LANG_DETECTION, languageDetection(CONTENT, 10000));

			// Get METAs
			HtmlElement head = page.getHead();
			if (head != null) {
				List<HtmlElement> metas = head.getElementsByTagName("meta");
				if (metas != null) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					for (DomElement meta : metas) {
						String name = meta.getAttribute("name");
						String content = meta.getAttribute("content");
						if (!StringUtils.isEmpty(name)
								&& !StringUtils.isEmpty(content))
							map.put(name, content);
					}
					if (!map.isEmpty())
						document.add(METAS, map);
				}
			}

		} finally {
			if (webClient != null)
				webClient.closeAllWindows();
		}

	}

	private void addToMap(Map<String, String> map, String name, String value) {
		if (!StringUtils.isEmpty(value))
			map.put(name, value);
	}

	private void addToField(ParserDocument document, ParserField parserField,
							DomNodeList<HtmlElement> elements) {
		if (elements == null)
			return;
		for (HtmlElement element : elements)
			document.add(parserField, element.asText());
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension,
								String mimeType) throws Exception {
		File tempFile = ParserAbstract.createTempFile(inputStream,
				extension == null ? "page.html" : "." + extension);
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
