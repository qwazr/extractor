/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.concurrent.FunctionEx;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public interface ParserUtils {

    static String getParameterValue(final MultivaluedMap<String, String> parameters,
                                    final ParserField param,
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

    static String getExtension(final Path filePath) {
        return filePath == null ? null : FilenameUtils.getExtension(filePath.getFileName().toString());
    }

    static Path createTempFile(final InputStream inputStream, final String extension) throws IOException {
        final Path tempFile = Files.createTempFile("qwazr-extractor", extension);
        try (final OutputStream out = Files.newOutputStream(tempFile);
             final BufferedOutputStream bOut = new BufferedOutputStream(out)) {
            IOUtils.copy(inputStream, bOut);
            bOut.close();
            return tempFile;
        }
    }

    static ParserResult toBufferedStream(final Path filePath,
                                         final FunctionEx<InputStream, ParserResult, IOException> streamConsumerEx) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(filePath)) {
            try (final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
                return streamConsumerEx.apply(bufferedInputStream);
            }
        }
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
    static String languageDetection(final ParserResult.Builder resultBuilder,
                                    final ParserField source,
                                    final int maxLength) {
        final StringBuilder sb = new StringBuilder();
        resultBuilder.forEachDocument(doc -> doc.extractField(source, maxLength, sb));
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
    static String languageDetection(final ParserResult.FieldsBuilder document,
                                    final ParserField source,
                                    final int maxLength) {
        final StringBuilder sb = new StringBuilder();
        document.extractField(source, maxLength, sb);
        return Language.quietDetect(sb.toString(), maxLength);
    }

}
