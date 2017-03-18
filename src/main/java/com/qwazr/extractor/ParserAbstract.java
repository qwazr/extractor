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

import com.qwazr.utils.Language;
import com.qwazr.utils.StringUtils;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class ParserAbstract implements ParserInterface {

	private final String name;

	protected ParserAbstract() {
		name = StringUtils.removeEnd(this.getClass().getSimpleName(), "Parser").toLowerCase();
	}

	protected String getParameterValue(final MultivaluedMap<String, String> parameters, final ParserField param,
			final int position) {
		if (parameters == null)
			return null;
		final List<String> values = parameters.get(param.name);
		if (values == null)
			return null;
		if (position >= values.size())
			return null;
		return values.get(position);
	}

	@Override
	public final String getName() {
		return name;
	}

	protected static File createTempFile(final InputStream inputStream, final String extension) throws IOException {
		final File tempFile = File.createTempFile("oss-extractor", extension);
		try (final FileOutputStream fos = new FileOutputStream(tempFile)) {
			IOUtils.copy(inputStream, fos);
			fos.close();
			return tempFile;
		}
	}

	protected void extractField(final ParserFieldsBuilder document, final ParserField source, final int maxLength,
			final StringBuilder sb) {
		if (sb.length() >= maxLength)
			return;
		final Object value = document.fields.get(source.name);
		if (value == null)
			return;
		if (value instanceof List) {
			for (Object object : (List) value) {
				if (object == null)
					continue;
				sb.append(object.toString());
				sb.append(' ');
				if (sb.length() >= maxLength)
					return;
			}
		} else
			sb.append(value.toString());
	}

	/**
	 * Submit the content of a field to language detection. It checks all the
	 * document.
	 *
	 * @param resultBuilder the documents to check
	 * @param source        The field to submit
	 * @param maxLength     The maximum number of characters
	 * @return the detected language
	 */
	protected final String languageDetection(final ParserResultBuilder resultBuilder, final ParserField source,
			final int maxLength) {
		final StringBuilder sb = new StringBuilder();
		resultBuilder.documentsBuilders.forEach(doc -> extractField(doc, source, maxLength, sb));
		return Language.quietDetect(sb.toString(), maxLength);
	}

	/**
	 * Submit the content if of a field to language detection.
	 *
	 * @param document  the document to check
	 * @param source    the field containing the text to match
	 * @param maxLength the maximum number of characters to test
	 * @return the detected language
	 */
	protected final String languageDetection(final ParserFieldsBuilder document, final ParserField source,
			final int maxLength) {
		final StringBuilder sb = new StringBuilder();
		extractField(document, source, maxLength, sb);
		return Language.quietDetect(sb.toString(), maxLength);
	}

}
