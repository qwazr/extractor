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

import com.qwazr.extractor.ParserFactory;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserInterface;
import com.qwazr.extractor.ParserResult;
import com.qwazr.extractor.ParserUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class RtfParser implements ParserFactory, ParserInterface {

    private final static String NAME = "rtf";

    private static final Map<String, MediaType> EXT_TYPES = Map.of(
            "rtf", MediaType.valueOf("application/rtf"),
            "rtx", MediaType.valueOf("text/richtext"));

    final private static List<ParserField> FIELDS = List.of(TITLE, CONTENT, LANG_DETECTION);

    @Override
    public Collection<ParserField> getParameters() {
        return null;
    }

    @Override
    public Collection<ParserField> getFields() {
        return FIELDS;
    }

    @Override
    public Collection<String> getSupportedFileExtensions() {
        return EXT_TYPES.keySet();
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
        return EXT_TYPES.values();
    }

    private ParserResult extract(final InputStream inputStream, ParserResult.Builder builder) throws IOException {
        try {
            // Extract the text data
            final RTFEditorKit rtf = new RTFEditorKit();
            final Document doc = rtf.createDefaultDocument();
            rtf.read(inputStream, doc, 0);

            // Obtain a new parser document.
            final ParserResult.FieldsBuilder result = builder.newDocument();

            result.add(TITLE, doc.getProperty(Document.TitleProperty));

            // Fill the field of the ParserDocument
            result.add(CONTENT, doc.getText(0, doc.getLength()));

            // Apply the language detection
            result.add(LANG_DETECTION, ParserUtils.languageDetection(result, CONTENT, 10000));

            return builder.build();
        } catch (BadLocationException e) {
            throw new IOException(e);
        }
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                final Path path) throws IOException {
        final ParserResult.Builder builder = ParserResult.of(NAME);
        builder.metas().set(MIME_TYPE, EXT_TYPES.get(ParserUtils.getExtension(path)));
        return ParserUtils.toBufferedStream(path, input -> extract(input, builder));
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                final InputStream inputStream,
                                final MediaType mimeType) throws IOException {
        final ParserResult.Builder builder = ParserResult.of(NAME);
        builder.metas().set(MIME_TYPE, mimeType.toString());
        return extract(inputStream, builder);
    }

}
