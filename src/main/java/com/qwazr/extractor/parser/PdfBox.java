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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class PdfBox extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = { "application/pdf" };

	public static final String[] DEFAULT_EXTENSIONS = { "pdf" };

	final protected static ParserField TITLE = ParserField.newString("title", "The title of the Word document");

	final protected static ParserField AUTHOR = ParserField.newString("author", "The name of the author");

	final protected static ParserField SUBJECT = ParserField.newString("subject", "The subject of the document");

	final protected static ParserField CONTENT = ParserField.newString("content", "The content of the document");

	final protected static ParserField PRODUCER = ParserField.newString("producer", "The producer of the document");

	final protected static ParserField KEYWORDS = ParserField.newString("keywords", "The keywords of the document");

	final protected static ParserField CREATION_DATE = ParserField.newDate("creation_date", null);

	final protected static ParserField MODIFICATION_DATE = ParserField.newDate("modification_date", null);

	final protected static ParserField LANGUAGE = ParserField.newString("language", null);

	final protected static ParserField ROTATION = ParserField.newInteger("rotation", null);

	final protected static ParserField NUMBER_OF_PAGES = ParserField.newInteger("number_of_pages", null);

	final protected static ParserField CHARACTER_COUNT = ParserField.newInteger("character_count", null);

	final protected static ParserField LANG_DETECTION =
			ParserField.newString("lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS =
			{ TITLE, AUTHOR, SUBJECT, CONTENT, PRODUCER, KEYWORDS, CREATION_DATE, MODIFICATION_DATE, LANGUAGE, ROTATION,
					NUMBER_OF_PAGES, LANG_DETECTION };

	final protected static ParserField PASSWORD = ParserField.newString("password", StringUtils.EMPTY);

	final protected static ParserField[] PARAMETERS = { PASSWORD };

	public PdfBox() {
	}

	private void extractMetaData(PDDocument pdf) throws IOException {
		PDDocumentInformation info = pdf.getDocumentInformation();
		if (info != null) {
			metas.add(TITLE, info.getTitle());
			metas.add(SUBJECT, info.getSubject());
			metas.add(AUTHOR, info.getAuthor());
			metas.add(PRODUCER, info.getProducer());
			metas.add(KEYWORDS, info.getKeywords());
			metas.add(CREATION_DATE, info.getCreationDate());
			metas.add(MODIFICATION_DATE, info.getModificationDate());
		}
		int pages = pdf.getNumberOfPages();
		metas.add(NUMBER_OF_PAGES, pages);
		PDDocumentCatalog catalog = pdf.getDocumentCatalog();
		if (catalog != null)
			metas.add(LANGUAGE, catalog.getLanguage());
	}

	/**
	 * Extract text content using PDFBox
	 *
	 * @param pdf
	 * @throws Exception
	 */
	private void parseContent(PDDocument pdf) throws Exception {
		try {
			extractMetaData(pdf);
			Stripper stripper = new Stripper();
			stripper.getText(pdf);
		} finally {
			if (pdf != null)
				pdf.close();
		}
	}

	private String getPassword() {
		final String password = getParameterValue(PASSWORD, 0);
		return password == null ? StringUtils.EMPTY : password;
	}

	@Override
	public void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception {
		parseContent(PDDocument.load(inputStream, getPassword()));
	}

	@Override
	public void parseContent(File file, String extension, String mimeType) throws Exception {
		parseContent(PDDocument.load(file, getPassword()));
	}

	@Override
	protected ParserField[] getParameters() {
		return PARAMETERS;
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

	public class Stripper extends PDFTextStripper {

		public Stripper() throws IOException {
			super();
		}

		@Override
		protected void endPage(PDPage page) throws IOException {
			super.endPage(page);
			ParserDocument document = getNewParserDocument();
			String text = output.toString();
			document.add(CHARACTER_COUNT, text.length());
			document.add(CONTENT, text);
			document.add(LANG_DETECTION, languageDetection(CONTENT, 10000));
			document.add(ROTATION, page.getRotation());
			output = new StringWriter();
		}
	}
}
