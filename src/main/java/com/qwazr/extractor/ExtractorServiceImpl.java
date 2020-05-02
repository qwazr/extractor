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
import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import org.apache.commons.io.FilenameUtils;

import javax.validation.constraints.NotNull;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ExtractorServiceImpl extends AbstractServiceImpl implements ExtractorServiceInterface {

    private static final Logger LOGGER = LoggerUtils.getLogger(ExtractorServiceImpl.class);

    private final ExtractorManager extractorManager;

    ExtractorServiceImpl(ExtractorManager extractorManager) {
        this.extractorManager = extractorManager;
    }

    @Override
    public Set<String> list() {
        return new TreeSet<>(extractorManager.getList());
    }

    @NotNull
    private ParserInterface checkParserNotNull(final ParserInterface parserInterface, final Supplier<String> parserId) {
        if (parserInterface == null)
            throw new NotFoundException("Parser not found: " + parserId.get());
        return parserInterface;
    }

    @NotNull
    private ParserInterface getParser(final Class<? extends ParserInterface> parserClass) throws ServerException {
        return checkParserNotNull(extractorManager.findParserClass(parserClass), parserClass::toString);
    }

    @NotNull
    private ParserInterface getParser(String parserName) {
        return checkParserNotNull(extractorManager.findParserClassByName(parserName), parserName::toString);
    }

    private Path getFilePath(final String path) {
        final Path filePath = Paths.get(path);
        if (!Files.exists(filePath))
            throw new NotFoundException("File not found: " + path);
        if (!Files.isRegularFile(filePath))
            throw new NotAcceptableException("The path is not a regular file: " + path);
        return filePath;
    }

    @Override
    public Object get(UriInfo uriInfo, String parserName, String path) {
        try {
            final ParserInterface parser = getParser(parserName);
            if (path == null)
                return new ParserDefinition(parser);
            final Path filePath = getFilePath(path);
            final ParserResultBuilder result = new ParserResultBuilder(parser);
            parser.parseContent(getQueryParameters(uriInfo), filePath, null, null, result);
            return result.build();
        }
        catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    private Path checkPathIsRegularFile(final Path filePath) throws ServerException {
        if (filePath == null)
            throw new NotAcceptableException("The path is missing");
        if (!Files.isRegularFile(filePath))
            throw new NotAcceptableException("The path is not a regular file: " + filePath.toAbsolutePath());
        return filePath;
    }

    private MultivaluedMap<String, String> getQueryParameters(UriInfo uriInfo) {
        return uriInfo == null ? null : uriInfo.getQueryParameters();
    }

    private ParserResult extract(final ParserInterface parser,
                                 final MultivaluedMap<String, String> parameters,
                                 final InputStream inputStream) {
        final ParserResultBuilder result = new ParserResultBuilder(parser);
        parser.parseContent(parameters, inputStream, null, null, result);
        return result.build();
    }

    @Override
    public ParserResult extract(final Class<? extends ParserInterface> parserClass,
                                final MultivaluedMap<String, String> parameters,
                                final InputStream inputStream) {
        final ParserInterface parser = getParser(parserClass);
        return extract(parser, parameters, inputStream);
    }

    private ParserResult extract(final ParserInterface parser,
                                 final MultivaluedMap<String, String> parameters,
                                 final Path filePath) {
        checkPathIsRegularFile(filePath);
        final ParserResultBuilder result = new ParserResultBuilder(parser);
        parser.parseContent(parameters, filePath, null, null, result);
        return result.build();
    }

    @Override
    public ParserResult extract(final Class<? extends ParserInterface> parserClass,
                                final MultivaluedMap<String, String> parameters,
                                final Path filePath) {
        final ParserInterface parser = getParser(parserClass);
        return extract(parser, parameters, filePath);
    }

    @Override
    public ParserResult put(UriInfo uriInfo, String parserName, String filePath, InputStream inputStream) {
        final MultivaluedMap<String, String> parameters = getQueryParameters(uriInfo);
        final ParserInterface parserInterface = extractorManager.findParserClassByName(parserName);
        if (filePath != null)
            return extract(parserInterface, parameters, Path.of(filePath));
        else if (inputStream != null)
            return extract(parserInterface, parameters, inputStream);
        else
            throw new NotAcceptableException("Both the file path and inputstream are null.");
    }

    private ParserInterface getClassParserExtension(String extension) {
        return StringUtils.isEmpty(extension) ? null : extractorManager.findParserClassByExtensionFirst(extension);
    }

    private ParserInterface getClassParserMimeType(String mimeType) {
        return StringUtils.isEmpty(mimeType) ? null : extractorManager.findParserClassByMimeTypeFirst(mimeType);
    }

    private String getMimeMagic(Path filePath) {
        try {
            final MagicMatch match = Magic.getMagicMatch(filePath.toFile(), true, true);
            if (match == null)
                return null;
            return match.getMimeType();
        }
        catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
            return null;
        }
    }

    private ParserResult putMagicPath(final MultivaluedMap<String, String> queryParameters,
                                      final String filePath,
                                      String mimeType) {

        final Path path = getFilePath(filePath);

        // Find a parser with the extension
        final String extension = FilenameUtils.getExtension(path.getFileName().toString());
        ParserInterface parserInterface = getClassParserExtension(extension);

        // Find a parser with the mimeType
        if (parserInterface == null) {
            if (StringUtils.isEmpty(mimeType))
                mimeType = getMimeMagic(path);
            if (!StringUtils.isEmpty(mimeType))
                parserInterface = getClassParserMimeType(mimeType);
        }

        // Do the extraction
        return extract(parserInterface, queryParameters, path);
    }

    private ParserResult putMagicStream(final MultivaluedMap<String, String> queryParameters,
                                        final String fileName, String mimeType,
                                        final InputStream inputStream) {

        Path tempFile = null;
        try {
            ParserInterface parser = null;

            // Find a parser with the extension
            String extension = null;
            if (!StringUtils.isEmpty(fileName)) {
                extension = FilenameUtils.getExtension(fileName);
                parser = getClassParserExtension(extension);
            }

            // Find a parser from the mime type
            if (parser == null) {
                if (StringUtils.isEmpty(mimeType)) {
                    tempFile = Files.createTempFile("textextractor",
                            extension == null ? StringUtils.EMPTY : "." + extension);
                    try {
                        IOUtils.copy(inputStream, tempFile);
                    }
                    finally {
                        IOUtils.closeQuietly((AutoCloseable) inputStream);
                    }
                    mimeType = getMimeMagic(tempFile);
                }
                if (!StringUtils.isEmpty(mimeType))
                    parser = getClassParserMimeType(mimeType);
            }

            // Do the extraction
            if (tempFile != null)
                return extract(parser, queryParameters, tempFile);
            else
                return extract(parser, queryParameters, inputStream);
        }
        catch (IOException e) {
            throw new InternalServerErrorException("An I/O error occurred: " + e, e);
        }
        finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                }
                catch (IOException e) {
                    LOGGER.log(Level.WARNING, e, e::getMessage);
                }
            }
        }
    }

    @Override
    public ParserResult extractMagic(final MultivaluedMap<String, String> parameters,
                                     final String fileName,
                                     final String filePath,
                                     final String mimeType,
                                     final InputStream inputStream) {
        if (filePath != null)
            return putMagicPath(parameters, filePath, mimeType);
        else if (inputStream != null)
            return putMagicStream(parameters, fileName, mimeType, inputStream);
        else
            throw new NotAcceptableException("Both the file path and inputstream are null.");
    }

    @Override
    public ParserResult putMagic(final UriInfo uriInfo,
                                 final String fileName,
                                 final String filePath,
                                 final String mimeType,
                                 final InputStream inputStream) {
        return extractMagic(getQueryParameters(uriInfo), fileName, filePath, mimeType, inputStream);
    }

}
