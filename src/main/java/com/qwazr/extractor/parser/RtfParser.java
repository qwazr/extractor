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

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserFieldsBuilder;
import com.qwazr.extractor.ParserResultBuilder;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;

public class RtfParser extends ParserAbstract {

    private static final String[] DEFAULT_MIMETYPES = {"application/rtf", "text/richtext"};

    private static final String[] DEFAULT_EXTENSIONS = {"rtf", "rtx"};

    final private static ParserField[] FIELDS = {TITLE, CONTENT, LANG_DETECTION};

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
                             String extension, final String mimeType, final ParserResultBuilder resultBuilder) {

        try {
            // Extract the text data
            final RTFEditorKit rtf = new RTFEditorKit();
            final Document doc = rtf.createDefaultDocument();
            rtf.read(inputStream, doc, 0);

            resultBuilder.metas().set(MIME_TYPE, findMimeType(extension, mimeType, this::findMimeTypeUsingDefault));

            // Obtain a new parser document.
            final ParserFieldsBuilder result = resultBuilder.newDocument();

            result.add(TITLE, doc.getProperty(Document.TitleProperty));

            // Fill the field of the ParserDocument
            result.add(CONTENT, doc.getText(0, doc.getLength()));

            // Apply the language detection
            result.add(LANG_DETECTION, languageDetection(result, CONTENT, 10000));

        }
        catch (IOException e) {
            throw convertIOException(e::getMessage, e);
        }
        catch (BadLocationException e) {
            throw convertException(e::getMessage, e);
        }

    }

}
