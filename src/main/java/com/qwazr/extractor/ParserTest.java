/*
 * Copyright 2015-2020 Emmanuel Keller
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

import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.ObjectMappers;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

public class ParserTest {

    static final Logger LOGGER = LoggerUtils.getLogger(ParserTest.class);

    protected final ExtractorManager manager;
    protected final ExtractorServiceInterface service;

    public ParserTest(final ExtractorManager manager) {
        this.manager = manager;
        this.service = manager.getService();
    }


    protected InputStream getStream(String fileName) {
        InputStream inputStream = getClass().getResourceAsStream(fileName);
        assert (inputStream != null);
        return inputStream;
    }

    protected Path getTempFile(String fileName) throws IOException {
        Path tempFile = Files.createTempFile("oss_extractor", "." + FilenameUtils.getExtension(fileName));
        try (final OutputStream out = Files.newOutputStream(tempFile);
             final BufferedOutputStream bOut = new BufferedOutputStream(out)) {
            InputStream inputStream = getStream(fileName);
            IOUtils.copy(inputStream, bOut);
        }
        return tempFile;
    }

    /**
     * Check if the given string is present in a map
     *
     * @param map  the map to check
     * @param text the text to find into the map
     * @return true if the text is find in the map values
     */
    protected boolean checkMapContainsText(Map<String, Object> map, String text) {
        for (Object value : map.values())
            if (checkContainsTextValue(value, text))
                return true;
        return false;
    }

    protected boolean checkCollectionContainsText(Collection<?> collection, String text) {
        for (Object value : collection)
            if (checkContainsTextValue(value, text))
                return true;
        return false;
    }

    protected boolean checkContainsTextValue(Object value, String text) {
        if (value == null)
            return false;
        if (value instanceof Collection)
            return checkCollectionContainsText((Collection<?>) value, text);
        if (value instanceof Map)
            return checkMapContainsText((Map) value, text);
        return value.toString().contains(text);
    }

    /**
     * Check if the given string is present in the result
     *
     * @param result    the ParserResult to check
     * @param fieldName The field to look at in the ParserResult
     * @param text      the text to look at
     */
    protected void checkContainsText(ParserResult result, String fieldName, String text) {
        if (text == null)
            return;
        if (checkContainsTextValue(result.documents, text)) {
            if (fieldName != null)
                assert result.getDocumentFieldValue(0, fieldName, 0) != null;
            return;
        }
        if (checkContainsTextValue(result.metas, text)) {
            if (fieldName != null)
                assert result.getDocumentFieldValue(0, fieldName, 0) != null;
            return;
        }
        assert false;
    }

    protected void checkIsMimeType(ParserFactory factory, ParserResult result, MediaType expectedMimeType) {
        assert result != null;
        assert result.metas != null;
        final Object mimeType = result.metas.get("mime_type");
        assert mimeType != null;
        assert mimeType instanceof String;
        assert mimeType.equals(expectedMimeType.toString());
        if (factory.getSupportedMimeTypes() != null)
            assert factory.getSupportedMimeTypes().contains(expectedMimeType);
    }

    /**
     * Test inputstream and file parsing
     *
     * @param factoryClassName the class to test
     * @param expectedMimeType the expected Mime type to find
     * @param expectedField    the expected field to find
     * @param expectedText     the expected text to find
     * @param fileName         the filename of the file to extract
     * @param keyValueParams   the parameters to apply
     * @return the ParserResult
     * @throws URISyntaxException if any URL syntax error occurs
     * @throws IOException        if any I/O error occurs
     */
    protected ParserResult doTest(Class<? extends ParserFactory> factoryClassName,
                                  String fileName,
                                  MediaType expectedMimeType,
                                  String expectedField,
                                  String expectedText,
                                  String... keyValueParams) throws URISyntaxException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        LOGGER.info("Testing " + factoryClassName);

        UriBuilder uriBuilder = new JerseyUriBuilder().uri("http://localhost:9090");
        for (int i = 0; i < keyValueParams.length; i += 2)
            uriBuilder.queryParam(keyValueParams[i], keyValueParams[i + 1]);
        final UriInfo uriInfo = new UriInfoImpl(new URI("http://localhost:9090"), uriBuilder.build());

        final ParserFactory factory = factoryClassName.getConstructor().newInstance();
        final String parserName = factory.getName();

        // Test service name
        assert service.getParserNames().contains(factory.getName());

        // Check ParserDefinition
        final ParserDefinition parserDefinition = service.getParserDefinition(parserName);
        assert parserDefinition != null;
        if (expectedMimeType != null && parserDefinition.mimeTypes != null)
            assert parserDefinition.mimeTypes.contains(expectedMimeType.toString());

        final ParserDefinition serialParserDefinition =
                ObjectMappers.JSON.readValue(ObjectMappers.JSON.writeValueAsString(parserDefinition),
                        ParserDefinition.class);
        assert Objects.equals(parserDefinition, serialParserDefinition);

        Path tempFile = getTempFile(fileName);

        ParserResult parserResult;

        {  // Test stream
            ParserInterface parser = factory.createParser();
            parserResult = parser.extract(uriInfo.getQueryParameters(), getStream(fileName), expectedMimeType);
            assert (parserResult != null);
            checkIsMimeType(factory, parserResult, expectedMimeType);
            checkContainsText(parserResult, expectedField, expectedText);
        }

        { // Test file
            ParserInterface parser = factory.createParser();
            parserResult = parser.extract(uriInfo.getQueryParameters(), tempFile);
            assert (parserResult != null);
            checkContainsText(parserResult, expectedField, expectedText);
        }

        // No magic to test if the parser doesn't support detection
        if (expectedMimeType != null && factory.getSupportedMimeTypes() == null && factory.getSupportedFileExtensions() == null)
            return parserResult;

        // Test stream with magic mime service

        parserResult = service.extractStream(uriInfo,
                new HttpHeadersImpl(Map.of(HttpHeaders.CONTENT_TYPE, expectedMimeType.toString())),
                getStream(fileName));
        assert (parserResult != null);
        checkContainsText(parserResult, expectedField, expectedText);

        // Test path with magic mime service
        parserResult = service.extractFile(uriInfo, tempFile.toAbsolutePath().toString());
        assert (parserResult != null);
        checkContainsText(parserResult, expectedField, expectedText);

        return parserResult;
    }


}
