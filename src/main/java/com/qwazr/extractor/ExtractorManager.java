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

import com.qwazr.extractor.parser.*;

import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ExtractorManager {

	public final static String SERVICE_NAME_EXTRACTOR = "extractor";

	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Map<String, Class<? extends ParserAbstract>> namesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserAbstract>> mimeTypesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserAbstract>> extensionsMap;

	static ExtractorManager INSTANCE = null;

	public synchronized static Class<? extends ExtractorServiceImpl> load() throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new ExtractorManager();
		return ExtractorServiceImpl.class;
	}

	public static ExtractorManager getInstance() {
		if (ExtractorManager.INSTANCE == null)
			throw new RuntimeException("The extractor service is not enabled");
		return ExtractorManager.INSTANCE;
	}

	private ExtractorManager() {
		namesMap = new LinkedHashMap<String, Class<? extends ParserAbstract>>();
		mimeTypesMap = new MultivaluedHashMap<String, Class<? extends ParserAbstract>>();
		extensionsMap = new MultivaluedHashMap<String, Class<? extends ParserAbstract>>();

		register(Audio.class);
		register(Doc.class);
		register(Docx.class);
		register(Eml.class);
		register(Html.class);
		register(Image.class);
		register(MapiMsg.class);
		register(Markdown.class);
		register(Odf.class);
		register(PdfBox.class);
		register(Ppt.class);
		register(Pptx.class);
		register(Publisher.class);
		register(Rss.class);
		register(Rtf.class);
		register(Text.class);
		register(Visio.class);
		register(Xls.class);
		register(Xlsx.class);
	}

	public final void register(Class<? extends ParserAbstract> parserClass) {
		Lock l = rwl.writeLock();
		l.lock();
		try {
			ParserAbstract parser = parserClass.newInstance();
			namesMap.put(parser.getName(), parserClass);
			String[] extensions = parser.getDefaultExtensions();
			if (extensions != null)
				for (String extension : extensions)
					extensionsMap.add(extension.intern(), parserClass);
			String[] mimeTypes = parser.getDefaultMimeTypes();
			if (mimeTypes != null)
				for (String mimeType : mimeTypes)
					mimeTypesMap.add(mimeType.intern(), parserClass);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} finally {
			l.unlock();
		}
	}

	public final Class<? extends ParserAbstract> findParserClassByName(String parserName) {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return namesMap.get(parserName);
		} finally {
			l.unlock();
		}
	}

	public final Class<? extends ParserAbstract> findParserClassByMimeTypeFirst(String mimeType) {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return mimeTypesMap.getFirst(mimeType);
		} finally {
			l.unlock();
		}
	}

	public final Class<? extends ParserAbstract> findParserClassByExtensionFirst(String extension) {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return extensionsMap.getFirst(extension);
		} finally {
			l.unlock();
		}
	}

	public final Set<String> getList() {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return namesMap.keySet();
		} finally {
			l.unlock();
		}
	}

}
