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

import com.qwazr.utils.ClassLoaderUtils;
import com.qwazr.utils.LoggerUtils;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.core.MultivaluedHashMap;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

public class ExtractorManager {

    private final static Logger LOGGER = LoggerUtils.getLogger(ExtractorManager.class);

    private final ParserMap parserMap;

    private volatile ParserMap volatileParserMap;

    private final ExtractorServiceInterface service;

    public ExtractorManager() {
        parserMap = new ParserMap();
        volatileParserMap = new ParserMap(parserMap);
        service = new ExtractorServiceImpl(this);
    }

    public ExtractorManager registerServices() {
        ServiceLoader.load(ParserInterface.class, Thread.currentThread().getContextClassLoader())
                .forEach(this::register);
        return this;
    }

    public ExtractorServiceInterface getService() {
        return service;
    }

    private synchronized void register(final ParserInterface parser) {
        volatileParserMap = parserMap.register(parser);
    }

    final public void register(Class<? extends ParserInterface> parserClass) {
        try {
            register(parserClass.getDeclaredConstructor().newInstance());
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new InternalServerErrorException("Cannot create an instance of " + parserClass, e);
        }
    }

    final public void register(String className) throws ClassNotFoundException {
        if (className == null)
            throw new NotAcceptableException("The classname is missing");
        register(ClassLoaderUtils.findClass(className));
    }

    final public ParserInterface findParserClass(final Class<? extends ParserInterface> parserClass) {
        if (parserClass == null)
            throw new NotAcceptableException("The parserClass is missing");
        return volatileParserMap.classesMap.get(parserClass);
    }

    final public ParserInterface findParserClassByName(String parserName) {
        if (parserName == null)
            throw new NotAcceptableException("The parserName is missing");
        return volatileParserMap.namesMap.get(parserName);
    }

    final public ParserInterface findParserClassByMimeTypeFirst(String mimeType) {
        if (mimeType == null)
            throw new NotAcceptableException("The mimeType is missing");
        return volatileParserMap.mimeTypesMap.getFirst(mimeType);
    }

    final public ParserInterface findParserClassByExtensionFirst(String extension) {
        if (extension == null)
            throw new NotAcceptableException("The extension is missing");
        return volatileParserMap.extensionsMap.getFirst(extension);
    }

    final public Set<String> getList() {
        return volatileParserMap.namesMap.keySet();
    }

    private static class ParserMap {

        private final Map<Class<? extends ParserInterface>, ParserInterface> classesMap;

        private final Map<String, ParserInterface> namesMap;

        private final MultivaluedHashMap<String, ParserInterface> mimeTypesMap;

        private final MultivaluedHashMap<String, ParserInterface> extensionsMap;

        private ParserMap() {
            classesMap = new HashMap<>();
            namesMap = new LinkedHashMap<>();
            mimeTypesMap = new MultivaluedHashMap<>();
            extensionsMap = new MultivaluedHashMap<>();
        }

        private ParserMap(final ParserMap parserMap) {
            this.classesMap = Map.copyOf(parserMap.classesMap);
            this.namesMap = Map.copyOf(parserMap.namesMap);
            this.mimeTypesMap = new MultivaluedHashMap<>(parserMap.mimeTypesMap);
            this.extensionsMap = new MultivaluedHashMap<>(parserMap.extensionsMap);
        }

        private synchronized ParserMap register(final ParserInterface parser) {
            final Class<? extends ParserInterface> parserClass = parser.getClass();
            final String parserName = parser.getName().intern();
            LOGGER.info(() -> "Registering " + parserName + " " + parserClass);
            classesMap.put(parserClass, parser);
            namesMap.put(parserName, parser);
            final String[] extensions = parser.getDefaultExtensions();
            if (extensions != null)
                for (String extension : extensions)
                    extensionsMap.add(extension.intern(), parser);
            final String[] mimeTypes = parser.getDefaultMimeTypes();
            if (mimeTypes != null)
                for (String mimeType : mimeTypes)
                    mimeTypesMap.add(mimeType.intern(), parser);
            return new ParserMap(this);
        }
    }

}
