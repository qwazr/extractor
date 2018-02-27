/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.LinkedHashMap;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class ExtractorManager {

	private final static Logger LOGGER = LoggerUtils.getLogger(ExtractorManager.class);

	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	private final LinkedHashMap<String, Class<? extends ParserInterface>> namesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserInterface>> mimeTypesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserInterface>> extensionsMap;

	private final ExtractorServiceInterface service;

	public ExtractorManager() {

		namesMap = new LinkedHashMap<>();
		mimeTypesMap = new MultivaluedHashMap<>();
		extensionsMap = new MultivaluedHashMap<>();

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

	private void register(final ParserInterface parser) {
		Lock l = rwl.writeLock();
		l.lock();
		try {
			final Class<? extends ParserInterface> parserClass = parser.getClass();
			LOGGER.info(() -> "Registering " + parserClass);
			namesMap.put(parser.getName(), parserClass);
			final String[] extensions = parser.getDefaultExtensions();
			if (extensions != null)
				for (String extension : extensions)
					extensionsMap.add(extension.intern(), parserClass);
			final String[] mimeTypes = parser.getDefaultMimeTypes();
			if (mimeTypes != null)
				for (String mimeType : mimeTypes)
					mimeTypesMap.add(mimeType.intern(), parserClass);
		} finally {
			l.unlock();
		}
	}

	final public void register(Class<? extends ParserInterface> parserClass) {
		try {
			register(parserClass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	final public void register(String className) throws ClassNotFoundException {
		register(ClassLoaderUtils.findClass(className));
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
