/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import org.apache.commons.io.FilenameUtils;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public interface ParserInterface {

	/**
	 * @return the parser name
	 */
	String getName();

	/**
	 * @return the parameters of the parser
	 */
	default ParserField[] getParameters() {
		return null;
	}

	/**
	 * @return the fields returned by this parser
	 */
	default ParserField[] getFields() {
		return null;
	}

	/**
	 * @return the list of supported extensions
	 */
	default String[] getDefaultExtensions() {
		return null;
	}

	/**
	 * @return the list of supported mime types
	 */
	default String[] getDefaultMimeTypes() {
		return null;
	}

	/**
	 * Read a document and fill the resultBuilder.
	 *
	 * @param parameters    The optional parameters of the parser
	 * @param inputStream   a stream of the content to analyze
	 * @param extension     the optional extension of the file
	 * @param mimeType      the option mime type of the file
	 * @param resultBuilder the result builder to fill
	 * @throws Exception if any error occurs
	 */
	void parseContent(final MultivaluedMap<String, String> parameters, final InputStream inputStream,
			final String extension, final String mimeType, final ParserResultBuilder resultBuilder) throws Exception;

	/**
	 * Read a document and fill the resultBuilder.
	 *
	 * @param parameters    The optional parameters of the parser
	 * @param file          the file instance of the document to parse
	 * @param extension     an optional extension of the file
	 * @param mimeType      an optional mime type of the file
	 * @param resultBuilder the result builder to fill
	 * @throws Exception if any error occurs
	 */
	default void parseContent(final MultivaluedMap<String, String> parameters, final File file, String extension,
			final String mimeType, final ParserResultBuilder resultBuilder) throws Exception {
		if (extension == null)
			extension = FilenameUtils.getExtension(file.getName());
		try (final InputStream is = new FileInputStream(file)) {
			parseContent(parameters, is, extension, mimeType, resultBuilder);
		}
	}

}
