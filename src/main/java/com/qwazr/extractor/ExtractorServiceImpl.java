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
package com.qwazr.extractor;

import com.qwazr.server.AbstractServiceImpl;
import com.qwazr.utils.LoggerUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

final class ExtractorServiceImpl extends AbstractServiceImpl implements ExtractorServiceInterface {

    private static final Logger LOGGER = LoggerUtils.getLogger(ExtractorServiceImpl.class);

    private final ExtractorManager extractorManager;

    ExtractorServiceImpl(ExtractorManager extractorManager) {
        this.extractorManager = extractorManager;
    }

    @Override
    public Set<String> getParserNames() {
        return extractorManager.getParserNames();
    }

    @Override
    public ParserDefinition getParserDefinition(final String parserName) {
        final ParserDefinition parserDefinition = extractorManager.getParserDefinition(parserName);
        if (parserDefinition == null)
            throw new NotFoundException("Parser not found: " + parserName);
        return parserDefinition;
    }


    private MultivaluedMap<String, String> getQueryParameters(UriInfo uriInfo) {
        return uriInfo == null ? null : uriInfo.getQueryParameters();
    }

    @Override
    public ParserResult extractFile(final UriInfo uriInfo,
                                    final String filePath) {
        try {
            return extract(getQueryParameters(uriInfo), Path.of(filePath));
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while extracting file:" + e.getMessage(), e);
        }
    }

    @Override
    public ParserResult extractStream(final UriInfo uriInfo,
                                      final HttpHeaders headers,
                                      final InputStream inputStream) {
        try {
            return extract(getQueryParameters(uriInfo), inputStream, headers.getMediaType());
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while extracting stream:" + e.getMessage(), e);
        }
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                final InputStream inputStream,
                                final MediaType mimeType) throws IOException {
        return extractorManager.extract(parameters, inputStream, mimeType);
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                final Path filePath) throws IOException {
        return extractorManager.extract(parameters, filePath);
    }
}
