/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.server.GenericServer;
import com.qwazr.utils.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ExtractorManager {

	public final static String PARSER_JSON_RESOURCE_PATH = "com/qwazr/extractor/parsers.json";

	private final static Logger LOGGER = LoggerFactory.getLogger(ExtractorManager.class);

	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	private final LinkedHashMap<String, Class<? extends ParserInterface>> namesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserInterface>> mimeTypesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserInterface>> extensionsMap;

	private final ExtractorServiceInterface service;

	private final ClassLoaderManager classLoaderManager;

	public ExtractorManager(final ClassLoaderManager classLoaderManager) {

		this.classLoaderManager = classLoaderManager;

		namesMap = new LinkedHashMap<>();
		mimeTypesMap = new MultivaluedHashMap<>();
		extensionsMap = new MultivaluedHashMap<>();

		service = new ExtractorServiceImpl(this);

	}

	private final static TypeReference<List<String>> ListStringTypeRef = new TypeReference<List<String>>() {
	};

	public ExtractorManager registerByJsonResources(String... resourcePathes)
			throws IOException, ClassNotFoundException {
		if (resourcePathes == null || resourcePathes.length == 0)
			resourcePathes = new String[] { PARSER_JSON_RESOURCE_PATH };
		final ClassLoader classLoader =
				classLoaderManager == null ? getClass().getClassLoader() : classLoaderManager.getClassLoader();
		for (final String resourcePath : resourcePathes) {
			final Enumeration<URL> parsersJson = classLoader.getResources(resourcePath);
			if (parsersJson == null)
				continue;
			while (parsersJson.hasMoreElements()) {
				final URL parserJsonUrl = parsersJson.nextElement();
				LOGGER.info("Loading parser resource: {}", parserJsonUrl);
				try (final InputStream in = parserJsonUrl.openStream()) {
					final List<String> list = JsonMapper.MAPPER.readValue(in, ListStringTypeRef);
					if (list != null)
						for (String className : list)
							register(className);
				}
			}
		}
		return this;
	}

	public ExtractorManager registerContextAttribute(final GenericServer.Builder builder) {
		builder.contextAttribute(this);
		return this;
	}

	public ExtractorManager registerWebService(final GenericServer.Builder builder) {
		builder.webService(ExtractorServiceImpl.class);
		return registerContextAttribute(builder);
	}

	public ExtractorServiceInterface getService() {
		return service;
	}

	final public void register(Class<? extends ParserInterface> parserClass) {
		Lock l = rwl.writeLock();
		l.lock();
		try {
			LOGGER.info("Registering {}", parserClass);
			final ParserInterface parser = parserClass.newInstance();
			namesMap.put(parser.getName(), parserClass);
			final String[] extensions = parser.getDefaultExtensions();
			if (extensions != null)
				for (String extension : extensions)
					extensionsMap.add(extension.intern(), parserClass);
			final String[] mimeTypes = parser.getDefaultMimeTypes();
			if (mimeTypes != null)
				for (String mimeType : mimeTypes)
					mimeTypesMap.add(mimeType.intern(), parserClass);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		} finally {
			l.unlock();
		}
	}

	final public void register(String className) throws ClassNotFoundException {
		register((Class<? extends ParserInterface>) (classLoaderManager == null ?
				Class.forName(className) :
				classLoaderManager.findClass(className)));
	}

	final public Class<? extends ParserInterface> findParserClassByName(String parserName) {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return namesMap.get(parserName);
		} finally {
			l.unlock();
		}
	}

	final public Class<? extends ParserInterface> findParserClassByMimeTypeFirst(String mimeType) {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return mimeTypesMap.getFirst(mimeType);
		} finally {
			l.unlock();
		}
	}

	final public Class<? extends ParserInterface> findParserClassByExtensionFirst(String extension) {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return extensionsMap.getFirst(extension);
		} finally {
			l.unlock();
		}
	}

	final public Set<String> getList() {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return namesMap.keySet();
		} finally {
			l.unlock();
		}
	}

}
