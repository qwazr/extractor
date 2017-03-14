/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.extractor.parser.Audio;
import com.qwazr.extractor.parser.Eml;
import com.qwazr.extractor.parser.Html;
import com.qwazr.extractor.parser.Image;
import com.qwazr.extractor.parser.Markdown;
import com.qwazr.extractor.parser.Ocr;
import com.qwazr.extractor.parser.Odf;
import com.qwazr.extractor.parser.PdfBox;
import com.qwazr.extractor.parser.Rss;
import com.qwazr.extractor.parser.Rtf;
import com.qwazr.extractor.parser.Text;
import com.qwazr.server.GenericServer;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class ExtractorManager {

	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	private final LinkedHashMap<String, Class<? extends ParserInterface>> namesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserInterface>> mimeTypesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserInterface>> extensionsMap;

	private final ExtractorServiceInterface service;

	private final ClassLoaderManager classLoaderManager;

	ExtractorManager(final ClassLoaderManager classLoaderManager) {

		this.classLoaderManager = classLoaderManager;

		namesMap = new LinkedHashMap<>();
		mimeTypesMap = new MultivaluedHashMap<>();
		extensionsMap = new MultivaluedHashMap<>();

		register(Audio.class);
		register(Eml.class);
		register(Html.class);
		register(Image.class);
		register(Markdown.class);
		register(Ocr.class);
		register(Odf.class);
		register(PdfBox.class);
		register(Rss.class);
		register(Rtf.class);
		register(Text.class);

		service = new ExtractorServiceImpl(this);

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
			ParserInterface parser = parserClass.newInstance();
			namesMap.put(parser.getName(), parserClass);
			String[] extensions = parser.getDefaultExtensions();
			if (extensions != null)
				for (String extension : extensions)
					extensionsMap.add(extension.intern(), parserClass);
			String[] mimeTypes = parser.getDefaultMimeTypes();
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
