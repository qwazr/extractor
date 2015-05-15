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

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.SupportedFileFormat;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserField;

public class Audio extends ParserAbstract {

	public static final HashMap<String, String> MIMEMAP;

	public static final String[] DEFAULT_EXTENSIONS;
	public static final String[] DEFAULT_MIMETYPES;

	static {
		MIMEMAP = new HashMap<String, String>();
		MIMEMAP.put("audio/ogg".intern(), "ogg");
		MIMEMAP.put("audio/mpeg".intern(), "mpg");
		MIMEMAP.put("audio/mpeg3".intern(), "mp3");
		MIMEMAP.put("audio/flac".intern(), "flag");
		MIMEMAP.put("audio/mp4".intern(), "mp4");
		MIMEMAP.put("audio/vnd.rn-realaudio".intern(), "ra");
		MIMEMAP.put("audio/x-pn-realaudio".intern(), "ra");
		MIMEMAP.put("audio/x-realaudio".intern(), "ra");
		MIMEMAP.put("audio/wav".intern(), "wav");
		MIMEMAP.put("audio/x-wav".intern(), "wav");

		DEFAULT_MIMETYPES = MIMEMAP.keySet()
				.toArray(new String[MIMEMAP.size()]);
	}

	final protected static Map<FieldKey, ParserField> FIELDMAP;
	final protected static ParserField[] FIELDS;

	final protected static ParserField FORMAT;

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
			FIELDMAP.put(fieldKey,
					ParserField.newString(fieldKey.name().toLowerCase(), null));
		FIELDS = FIELDMAP.values().toArray(new ParserField[FIELDMAP.size()]);
		Arrays.sort(FIELDS, ParserField.COMPARATOR);
	}

	final protected static ParserField[] PARAMETERS = { FORMAT };

	public Audio() {
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

	@Override
	protected void parseContent(File file, String extension, String mimeType)
			throws Exception {
		AudioFile f = AudioFileIO.read(file);
		Tag tag = f.getTag();
		if (tag == null)
			return;
		if (tag.getFieldCount() == 0)
			return;
		for (Map.Entry<FieldKey, ParserField> entry : FIELDMAP.entrySet()) {
			List<TagField> tagFields = tag.getFields(entry.getKey());
			if (tagFields == null)
				continue;
			for (TagField tagField : tagFields) {
				if (!(tagField instanceof TagTextField))
					continue;
				metas.add(entry.getValue(),
						((TagTextField) tagField).getContent());
			}
		}
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension,
			String mimeType) throws Exception {
		String format = getParameterValue(FORMAT, 0);
		if (StringUtils.isEmpty(format))
			format = extension;
		if (StringUtils.isEmpty(format) && mimeType != null)
			format = MIMEMAP.get(mimeType.intern());
		if (StringUtils.isEmpty(format))
			throw new Exception("The format is not found");
		File tempFile = ParserAbstract
				.createTempFile(inputStream, '.' + format);
		try {
			parseContent(tempFile, extension, mimeType);
		} finally {
			tempFile.delete();
		}
	}

}
