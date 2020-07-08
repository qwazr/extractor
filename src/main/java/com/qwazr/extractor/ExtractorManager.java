/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>¬org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor;

import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.concurrent.FunctionEx;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class ExtractorManager implements ParserInterface, AutoCloseable {

    private final static Logger LOGGER = LoggerUtils.getLogger(ExtractorManager.class);

    private final ExtractorServiceInterface service;

    private final List<ParserLoader> parserLoaders;

    private final Map<MediaType, List<Parser>> mimeTypesMap;

    private final Map<String, List<Parser>> extensionsMap;

    private final SortedMap<String, ParserFactory> parserFactories;

    public ExtractorManager() {
        parserLoaders = Collections.synchronizedList(new ArrayList<>());
        mimeTypesMap = new ConcurrentHashMap<>();
        extensionsMap = new ConcurrentHashMap<>();
        parserFactories = Collections.synchronizedSortedMap(new TreeMap<>());
        service = new ExtractorServiceImpl(this);
    }

    public ExtractorServiceInterface getService() {
        return service;
    }

    public Set<String> getParserNames() {
        return parserFactories.keySet();
    }

    public ExtractorManager registerServices() {
        ServiceLoader
                .load(ParserFactory.class, Thread.currentThread().getContextClassLoader())
                .forEach(factory -> register(factory, new Parser(factory)));
        return this;
    }

    public ExtractorManager registerExternalServices(final Path classesPath, final Path libPath) throws IOException {
        final ParserLoader loader = new ParserLoader(classesPath, libPath);
        parserLoaders.add(loader);
        loader.apply(classLoader -> {
            ServiceLoader.
                    load(ParserFactory.class, classLoader)
                    .forEach(factory -> register(factory, new ParserWithClassloader(loader, factory)));
            return null;
        });
        return this;
    }

    private void register(final ParserFactory parserFactory, final Parser parser) {
        synchronized (parserFactories) {
            final Collection<String> extensions = parserFactory.getSupportedFileExtensions();
            if (extensions != null)
                for (final String extension : extensions)
                    extensionsMap.computeIfAbsent(extension,
                            e -> Collections.synchronizedList(new ArrayList<>()))
                            .add(parser);
            final Collection<MediaType> mimeTypes = parserFactory.getSupportedMimeTypes();
            if (mimeTypes != null)
                for (final MediaType mimeType : mimeTypes)
                    mimeTypesMap.computeIfAbsent(mimeType,
                            t -> Collections.synchronizedList(new ArrayList<>()))
                            .add(parser);
            parserFactories.put(parserFactory.getName(), parserFactory);
        }
    }

    public ParserDefinition getParserDefinition(String parserName) {
        final ParserFactory factory = parserFactories.get(parserName);
        return factory == null ? null : new ParserDefinition(factory);
    }

    @Override
    public synchronized void close() throws Exception {
        mimeTypesMap.clear();
        extensionsMap.clear();
        for (final ParserLoader parserLoader : parserLoaders)
            parserLoader.close();
        parserLoaders.clear();
    }

    private ParserResult tryParsers(final String info,
                                    final Collection<Parser> parsers,
                                    final FunctionEx<Parser, ParserResult, IOException> parserFunction) throws IOException {
        if (parsers == null || parsers.isEmpty())
            throw new NotAcceptableException("No parser found: " + info);
        List<Exception> exceptions = null;
        for (final Parser parser : parsers) {
            try {
                return parserFunction.apply(parser);
            } catch (Exception e) {
                if (exceptions == null)
                    exceptions = new ArrayList<>();
                exceptions.add(e);
            }
        }
        if (exceptions.size() == 1) {
            final Exception exception = exceptions.get(0);
            if (exception instanceof IOException)
                throw (IOException) exception;
        }
        for (final Exception exception : exceptions) {
            LOGGER.log(Level.WARNING, exception.getMessage(), exception);
        }
        throw new InternalServerErrorException("Every parser failed on '" + info + "'. See the emitted warning logs for details.");
    }

    private void checkPathIsRegularFile(final Path filePath) throws ServerException {
        if (filePath == null)
            throw new NotAcceptableException("The file path is missing");
        if (!Files.exists(filePath))
            throw new NotFoundException("File not found: " + filePath.toAbsolutePath());
        if (!Files.isRegularFile(filePath))
            throw new NotAcceptableException("The path is not a regular file: " + filePath.toAbsolutePath());
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                final InputStream inputStream,
                                final MediaType mimeType) throws IOException {
        return tryParsers(mimeType.toString(), mimeTypesMap.get(mimeType),
                parser -> parser.extract(parameters, inputStream, mimeType));
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                final Path filePath) throws IOException {
        checkPathIsRegularFile(filePath);
        final String extension = ParserUtils.getExtension(filePath);
        return tryParsers(extension, extensionsMap.get(extension),
                parser -> parser.extract(parameters, filePath));
    }

    private static class Parser implements ParserInterface {

        private final ParserFactory factory;

        private Parser(final ParserFactory factory) {
            this.factory = factory;
        }

        @Override
        public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                    final InputStream inputStream,
                                    final MediaType mimeType) throws IOException {
            return factory.createParser().extract(parameters, inputStream, mimeType);
        }

        @Override
        public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                    final Path filePath) throws IOException {
            return factory.createParser().extract(parameters, filePath);
        }
    }

    private static class ParserWithClassloader extends Parser {

        private final ParserLoader loader;

        private ParserWithClassloader(final ParserLoader loader,
                                      final ParserFactory factory) {
            super(factory);
            this.loader = loader;
        }

        @Override
        public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                    final InputStream inputStream,
                                    final MediaType mimeType) throws IOException {
            return loader.apply(classLoader -> super.extract(parameters, inputStream, mimeType));
        }

        @Override
        public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                    final Path filePath) throws IOException {
            return loader.apply(classLoader -> super.extract(parameters, filePath));
        }
    }
}
