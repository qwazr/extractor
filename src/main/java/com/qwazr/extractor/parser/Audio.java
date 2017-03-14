/**
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

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserFieldsBuilder;
import com.qwazr.extractor.ParserResultBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.SupportedFileFormat;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class Audio extends ParserAbstract {

	private static final HashMap<String, String> MIMEMAP;

	private static final String[] DEFAULT_EXTENSIONS;
	private static final String[] DEFAULT_MIMETYPES;

	static {
		MIMEMAP = new HashMap<>();
		MIMEMAP.put("audio/ogg", "ogg");
		MIMEMAP.put("audio/mpeg", "mpg");
		MIMEMAP.put("audio/mpeg3", "mp3");
		MIMEMAP.put("audio/flac", "flag");
		MIMEMAP.put("audio/mp4", "mp4");
		MIMEMAP.put("audio/vnd.rn-realaudio", "ra");
		MIMEMAP.put("audio/x-pn-realaudio", "ra");
		MIMEMAP.put("audio/x-realaudio", "ra");
		MIMEMAP.put("audio/wav", "wav");
		MIMEMAP.put("audio/x-wav", "wav");

		DEFAULT_MIMETYPES = MIMEMAP.keySet().toArray(new String[MIMEMAP.size()]);
	}

	private final static Map<FieldKey, ParserField> FIELDMAP;
	private final static ParserField[] FIELDS;

	private final static ParserField FORMAT;

	static {
		// Build the list of extension for the FORMAT parameter
		StringBuilder sb = new StringBuilder("Supported format: ");
		boolean first = true;
		DEFAULT_EXTENSIONS = new String[SupportedFileFormat.values().length];
		int i = 0;
		for (SupportedFileFormat sff : SupportedFileFormat.values()) {
			if (!first)
				sb.append(", ");
			else
				first = false;
			sb.append(sff.getFilesuffix());
			DEFAULT_EXTENSIONS[i++] = sff.getFilesuffix();
		}
		FORMAT = ParserField.newString("format", sb.toString());

		// Build the list of fields returned by the library
		FIELDMAP = new HashMap<FieldKey, ParserField>();
		for (FieldKey fieldKey : FieldKey.values())
			FIELDMAP.put(fieldKey, ParserField.newString(fieldKey.name().toLowerCase(), null));
		FIELDS = FIELDMAP.values().toArray(new ParserField[FIELDMAP.size()]);
		Arrays.sort(FIELDS, ParserField.COMPARATOR);
	}

	private final static ParserField[] PARAMETERS = { FORMAT };

	@Override
	public ParserField[] getParameters() {
		return PARAMETERS;
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
	public void parseContent(final MultivaluedMap<String, String> parameters, final File file, String extension,
			final String mimeType, final ParserResultBuilder resultBuilder) throws Exception {
		final AudioFile f = AudioFileIO.read(file);
		final Tag tag = f.getTag();
		if (tag == null)
			return;
		if (tag.getFieldCount() == 0)
			return;
		for (Map.Entry<FieldKey, ParserField> entry : FIELDMAP.entrySet()) {
			List<TagField> tagFields = tag.getFields(entry.getKey());
			if (tagFields == null)
				continue;
			final ParserFieldsBuilder metas = resultBuilder.metas();
			for (TagField tagField : tagFields) {
				if (!(tagField instanceof TagTextField))
					continue;
				metas.add(entry.getValue(), ((TagTextField) tagField).getContent());
			}
		}
	}

	@Override
	public void parseContent(final MultivaluedMap<String, String> parameters, final InputStream inputStream,
			final String extension, final String mimeType, final ParserResultBuilder resultBuilder) throws Exception {
		String format = getParameterValue(parameters, FORMAT, 0);
		if (StringUtils.isEmpty(format))
			format = extension;
		if (StringUtils.isEmpty(format) && mimeType != null)
			format = MIMEMAP.get(mimeType.intern());
		if (StringUtils.isEmpty(format))
			throw new Exception("The format is not found");
		final File tempFile = ParserAbstract.createTempFile(inputStream, '.' + format);
		try {
			parseContent(parameters, tempFile, extension, mimeType, resultBuilder);
		} finally {
			tempFile.delete();
		}
	}

}
