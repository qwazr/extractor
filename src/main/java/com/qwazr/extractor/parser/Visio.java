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
package com.qwazr.extractor.parser;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hpsf.SummaryInformation;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;

public class Visio extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = { "application/vnd.visio" };

	public static final String[] DEFAULT_EXTENSIONS = { "vsd" };

	final protected static ParserField TITLE = ParserField.newString("title",
			"The title of the document");

	final protected static ParserField AUTHOR = ParserField.newString("author",
			"The name of the author");

	final protected static ParserField CREATION_DATE = ParserField.newDate(
			"creation_date", null);

	final protected static ParserField MODIFICATION_DATE = ParserField.newDate(
			"modification_date", null);

	final protected static ParserField KEYWORDS = ParserField.newString(
			"keywords", null);

	final protected static ParserField SUBJECT = ParserField.newString(
			"subject", "The subject of the document");

	final protected static ParserField COMMENTS = ParserField.newString(
			"comments", null);

	final protected static ParserField CONTENT = ParserField.newString(
			"content", "The content of the document");

	final protected static ParserField LANG_DETECTION = ParserField.newString(
			"lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS = { TITLE, AUTHOR,
			CREATION_DATE, MODIFICATION_DATE, KEYWORDS, SUBJECT, CONTENT,
			LANG_DETECTION };

	public Visio() {
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
		VisioTextExtractor extractor = null;
		try {
			extractor = new VisioTextExtractor(inputStream);
			SummaryInformation info = extractor.getSummaryInformation();

			if (info != null) {
				metas.add(TITLE, info.getTitle());
				metas.add(AUTHOR, info.getAuthor());
				metas.add(SUBJECT, info.getSubject());
				metas.add(CREATION_DATE, info.getCreateDateTime());
				metas.add(MODIFICATION_DATE, info.getLastSaveDateTime());
				metas.add(CONTENT, info.getKeywords());
				metas.add(COMMENTS, info.getComments());
			}
			String[] texts = extractor.getAllText();
			if (texts == null)
				return;
			ParserDocument result = getNewParserDocument();
			for (String text : texts)
				result.add(CONTENT, text);
			result.add(LANG_DETECTION, languageDetection(CONTENT, 10000));
		} finally {
			if (extractor != null)
				IOUtils.closeQuietly(extractor);
		}
	}
}
