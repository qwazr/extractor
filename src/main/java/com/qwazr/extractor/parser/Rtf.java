/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
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
package com.qwazr.extractor.parser;

import java.io.InputStream;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;

public class Rtf extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = { "application/rtf",
			"text/richtext" };

	public static final String[] DEFAULT_EXTENSIONS = { "rtf", "rtx" };

	final protected static ParserField CONTENT = ParserField.newString(
			"content", "The content of the document");

	final protected static ParserField LANG_DETECTION = ParserField.newString(
			"lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS = { CONTENT, LANG_DETECTION };

	public Rtf() {
	}

	@Override
	protected ParserField[] getParameters() {
		return null;
	}

	@Override
	protected ParserField[] getFields() {
		return FIELDS;
	}

	@Override
	protected String[] getDefaultExtensions() {
		return DEFAULT_EXTENSIONS;
	}

	@Override
	protected String[] getDefaultMimeTypes() {
		return DEFAULT_MIMETYPES;
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension,
			String mimeType) throws Exception {

		// Extract the text data
		RTFEditorKit rtf = new RTFEditorKit();
		Document doc = rtf.createDefaultDocument();
		rtf.read(inputStream, doc, 0);

		// Obtain a new parser document.
		ParserDocument result = getNewParserDocument();

		// Fill the field of the ParserDocument
		result.add(CONTENT, doc.getText(0, doc.getLength()));

		// Apply the language detection
		result.add(LANG_DETECTION, languageDetection(CONTENT, 10000));

	}

}
