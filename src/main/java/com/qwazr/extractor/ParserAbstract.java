/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor;

import com.qwazr.utils.Language;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ParserAbstract {

	private final String name;
	protected final ParserDocument metas;
	private final ArrayList<ParserDocument> documents;
	protected MultivaluedMap<String, String> parameters;

	protected ParserAbstract() {
		name = this.getClass().getSimpleName().toLowerCase();
		documents = new ArrayList<ParserDocument>(0);
		metas = new ParserDocument();
		parameters = null;
	}

	protected ParserDocument getNewParserDocument() {
		ParserDocument document = new ParserDocument();
		documents.add(document);
		return document;
	}

	protected String getParameterValue(ParserField param, int position) {
		if (parameters == null)
			return null;
		List<String> values = parameters.get(param.name);
		if (values == null)
			return null;
		if (position >= values.size())
			return null;
		return values.get(position);
	}

	/**
	 * @return the parameters of the parser
	 */
	protected abstract ParserField[] getParameters();

	/**
	 * @return the fields returned by this parser
	 */
	protected abstract ParserField[] getFields();

	/**
	 * Read a document and fill the ParserDocument list.
	 *
	 * @param inputStream
	 *            a stream of the content to analyze
	 * @param extension
	 *            the optional extension of the file
	 * @param mimeType
	 *            the option mime type of the file
	 * @throws Exception
	 *             if any error occurs
	 */
	protected abstract void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception;

	/**
	 * @return the list of supported extensions
	 */
	protected abstract String[] getDefaultExtensions();

	/**
	 * @return the list of supported mime types
	 */
	protected abstract String[] getDefaultMimeTypes();

	/**
	 * Read a document and fill the ParserDocument list.
	 *
	 * @param file
	 *            the file instance of the document to parse
	 * @param extension
	 *            an optional extension of the file
	 * @param mimeType
	 *            an optional mime type of the file
	 * @throws Exception
	 *             if any error occurs
	 */
	protected void parseContent(File file, String extension, String mimeType) throws Exception {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			parseContent(is, extension, mimeType);
		} finally {
			if (is != null)
				IOUtils.closeQuietly(is);
		}
	}

	protected final static File createTempFile(InputStream inputStream, String extension) throws IOException {
		File tempFile = File.createTempFile("oss-extractor", extension);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(tempFile);
			IOUtils.copy(inputStream, fos);
			fos.close();
			fos = null;
			return tempFile;
		} finally {
			if (fos != null)
				IOUtils.closeQuietly(fos);
		}
	}

	public final ParserResult doParsing(MultivaluedMap<String, String> parameters, InputStream inputStream,
					String extension, String mimeType) throws Exception {
		this.parameters = parameters;
		parseContent(inputStream, extension, mimeType);
		return new ParserResult(name, metas, documents);
	}

	public final ParserResult doParsing(MultivaluedMap<String, String> parameters, File file, String extension,
					String mimeType) throws Exception {
		this.parameters = parameters;
		if (extension == null)
			extension = FilenameUtils.getExtension(file.getName());
		parseContent(file, extension, mimeType);
		return new ParserResult(name, metas, documents);
	}

	/**
	 * Submit the content of a field to language detection. It checks all the
	 * document.
	 *
	 * @param source
	 *            The field to submit
	 * @param maxLength
	 *            The maximum number of characters
	 * @return the detected language
	 */
	protected final String languageDetection(ParserField source, int maxLength) {
		StringBuilder sb = new StringBuilder();
		for (ParserDocument document : documents) {
			List<Object> objectList = document.fields.get(source.name);
			if (objectList == null)
				continue;
			for (Object object : objectList) {
				if (object == null)
					continue;
				sb.append(object.toString());
				sb.append(' ');
				if (sb.length() > maxLength)
					Language.quietDetect(sb.toString(), maxLength);
			}
		}
		return Language.quietDetect(sb.toString(), maxLength);
	}

	/**
	 * Submit the content if of a field to language detection.
	 *
	 * @param document
	 *            the document to check
	 * @param source
	 *            the field containing the text to match
	 * @param maxLength
	 *            the maximum number of characters to test
	 * @return the detected language
	 */
	protected final String languageDetection(ParserDocument document, ParserField source, int maxLength) {
		StringBuilder sb = new StringBuilder();
		List<Object> objectList = document.fields.get(source.name);
		if (objectList == null)
			return null;
		for (Object object : objectList) {
			if (object == null)
				continue;
			sb.append(object.toString());
			sb.append(' ');
			if (sb.length() > maxLength)
				Language.quietDetect(sb.toString(), maxLength);
		}
		return Language.quietDetect(sb.toString(), maxLength);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
