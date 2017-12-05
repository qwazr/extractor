/*
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

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserFieldsBuilder;
import com.qwazr.extractor.ParserResultBuilder;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TextParser extends ParserAbstract {

	private static final String[] DEFAULT_MIMETYPES = { "text/plain" };

	private static final String[] DEFAULT_EXTENSIONS = { "txt" };

	final private static ParserField CHARSET_DETECTION =
			ParserField.newString("charset_detection", "Detection of the charset");

	final private static ParserField[] FIELDS = { CONTENT, LANG_DETECTION, CHARSET_DETECTION };

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
	public void parseContent(final MultivaluedMap<String, String> parameters, final InputStream inputStream,
			String extension, final String mimeType, final ParserResultBuilder resultBuilder) throws IOException {

		resultBuilder.metas().set(MIME_TYPE, findMimeType(extension, mimeType, this::findMimeTypeUsingDefault));

		// Trying to detect the CHARSET of the stream
		final CharsetDetector detector = new CharsetDetector();

		try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
			detector.setText(bis);
			final CharsetMatch match = detector.detect();
			final ParserFieldsBuilder result = resultBuilder.newDocument();
			final String content;
			if (match != null) {
				content = match.getString();
				result.add(CHARSET_DETECTION, match.getName());
			} else {
				bis.reset();
				content = IOUtils.toString(bis, Charset.defaultCharset());
			}
			result.add(CONTENT, content);
			result.add(LANG_DETECTION, languageDetection(result, CONTENT, 10000));
		}
	}

}
