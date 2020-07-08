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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public interface ParserInterface {

    ParserField TITLE = ParserField.newString("title", "The optional title of the document");

    ParserField CONTENT = ParserField.newString("content", "The content of the document");

    ParserField LANG_DETECTION = ParserField.newString("lang_detection", "Detection of the language");

    ParserField MIME_TYPE = ParserField.newString("mime_type", "The mime type of the file");

    /**
     * Extract data from a stream and return the ParserResult.
     *
     * @param parameters The optional parameters of the parser
     * @param mimeType   an optional mime type of the file
     * @return ParserResult the parser result
     * @throws IOException if any I/O error occurs
     */
    ParserResult extract(final MultivaluedMap<String, String> parameters,
                         final InputStream inputStream,
                         final MediaType mimeType) throws IOException;


    /**
     * Read a file and extract the data.
     *
     * @param parameters The optional parameters of the parser
     * @param filePath   the path of the file instance of the document to parse
     * @return ParserResult the parser result
     * @throws IOException if any I/O error occurs
     */
    ParserResult extract(final MultivaluedMap<String, String> parameters,
                         final Path filePath) throws IOException;

}
