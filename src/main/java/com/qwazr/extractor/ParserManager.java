/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.core.MultivaluedHashMap;

import com.qwazr.extractor.parser.Audio;
import com.qwazr.extractor.parser.Doc;
import com.qwazr.extractor.parser.Docx;
import com.qwazr.extractor.parser.Eml;
import com.qwazr.extractor.parser.Html;
import com.qwazr.extractor.parser.Image;
import com.qwazr.extractor.parser.MapiMsg;
import com.qwazr.extractor.parser.Markdown;
import com.qwazr.extractor.parser.Odf;
import com.qwazr.extractor.parser.PdfBox;
import com.qwazr.extractor.parser.Ppt;
import com.qwazr.extractor.parser.Pptx;
import com.qwazr.extractor.parser.Publisher;
import com.qwazr.extractor.parser.Rss;
import com.qwazr.extractor.parser.Rtf;
import com.qwazr.extractor.parser.Text;
import com.qwazr.extractor.parser.Visio;
import com.qwazr.extractor.parser.Xls;
import com.qwazr.extractor.parser.Xlsx;

public class ParserManager {

	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Map<String, Class<? extends ParserAbstract>> namesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserAbstract>> mimeTypesMap;

	private final MultivaluedHashMap<String, Class<? extends ParserAbstract>> extensionsMap;

	public static ParserManager INSTANCE = null;

	public static void load() throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new ParserManager();
	}

	private ParserManager() {
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

	public final Class<? extends ParserAbstract> findParserClassByName(
			String parserName) {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return namesMap.get(parserName);
		} finally {
			l.unlock();
		}
	}

	public final Class<? extends ParserAbstract> findParserClassByMimeTypeFirst(
			String mimeType) {
		Lock l = rwl.readLock();
		l.lock();
		try {
			return mimeTypesMap.getFirst(mimeType);
		} finally {
			l.unlock();
		}
	}

	public final Class<? extends ParserAbstract> findParserClassByExtensionFirst(
			String extension) {
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
