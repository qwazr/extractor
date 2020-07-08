/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.extractor.ParserFactory;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserInterface;
import com.qwazr.extractor.ParserResult;
import static com.qwazr.extractor.ParserUtils.languageDetection;
import static com.qwazr.extractor.ParserUtils.toBufferedStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.IOUtils;

public class TextParser implements ParserFactory, ParserInterface {

    private static final String NAME = "text";

    private static final List<MediaType> DEFAULT_MIMETYPES = List.of(MediaType.TEXT_PLAIN_TYPE);

    private static final List<String> DEFAULT_EXTENSIONS = List.of("txt");

    final private static ParserField CHARSET_DETECTION =
            ParserField.newString("charset_detection", "Detection of the charset");

    final private static Collection<ParserField> FIELDS = List.of(CONTENT, LANG_DETECTION, CHARSET_DETECTION);

    @Override
    public Collection<ParserField> getFields() {
        return FIELDS;
    }

    @Override
    public Collection<String> getSupportedFileExtensions() {
        return DEFAULT_EXTENSIONS;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ParserInterface createParser() {
        return this;
    }

    @Override
    public Collection<MediaType> getSupportedMimeTypes() {
        return DEFAULT_MIMETYPES;
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                final InputStream inputStream,
                                final MediaType mimeType) throws IOException {
        final ParserResult.Builder builder = ParserResult.of(NAME);
        builder.metas().set(MIME_TYPE, mimeType.toString());

        // Trying to detect the CHARSET of the stream
        final CharsetDetector detector = new CharsetDetector();

        try (final BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            detector.setText(bis);
            final CharsetMatch match = detector.detect();
            final ParserResult.FieldsBuilder result = builder.newDocument();
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
        return builder.build();
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters, final Path filePath) throws IOException {
        return toBufferedStream(filePath, input -> extract(parameters, input, MediaType.TEXT_PLAIN_TYPE));
    }
}
