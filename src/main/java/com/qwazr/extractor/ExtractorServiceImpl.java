/*
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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;
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

	private ParserInterface getParser(final Class<? extends ParserInterface> parserClass) throws ServerException {
		try {
			if (parserClass == null)
				throw new ServerException(Status.NOT_FOUND, "No parser found.");
			return parserClass.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
			throw ServerException.of(e);
		}
	}

	private ParserInterface getParser(String parserName) throws ServerException {
		final Class<? extends ParserInterface> parserClass = extractorManager.findParserClassByName(parserName);
		if (parserClass == null)
			throw new ServerException(Status.NOT_FOUND, "Unknown parser: " + parserName);
		return getParser(parserClass);
	}

	private File getFilePath(String path) throws ServerException {
		final File file = new File(path);
		if (!file.exists())
			throw new ServerException(Status.NOT_FOUND, "File not found: " + path);
		if (!file.isFile())
			throw new ServerException(Status.NOT_ACCEPTABLE, "The path is not a file: " + path);
		return file;
	}

	@Override
	public Object get(UriInfo uriInfo, String parserName, String path) {
		try {
			final ParserInterface parser = getParser(parserName);
			if (path == null)
				return new ParserDefinition(parser);
			final File file = getFilePath(path);
			final ParserResultBuilder result = new ParserResultBuilder(parser);
			parser.parseContent(getQueryParameters(uriInfo), file, null, null, result);
			return result.build();
		} catch (Exception e) {
			throw ServerException.getJsonException(LOGGER, e);
		}
	}

	private boolean checkIsPath(String filePath, InputStream inputStream) throws ServerException {
		final boolean isPath = !StringUtils.isEmpty(filePath);
		if (!isPath && inputStream == null)
			throw new ServerException(Status.NOT_ACCEPTABLE, "Not path and no stream.");
		return isPath;
	}

	private MultivaluedMap<String, String> getQueryParameters(UriInfo uriInfo) {
		return uriInfo == null ? null : uriInfo.getQueryParameters();
	}

	@Override
	public ParserResult extract(final String parserName, MultivaluedMap<String, String> parameters, String filePath,
			InputStream inputStream) {
		try {
			final ParserInterface parser = getParser(parserName);
			final ParserResultBuilder result = new ParserResultBuilder(parser);
			if (checkIsPath(filePath, inputStream))
				parser.parseContent(parameters, getFilePath(filePath), null, null, result);
			else
				parser.parseContent(parameters, inputStream, null, null, result);
			return result.build();
		} catch (Exception e) {
			throw ServerException.getJsonException(LOGGER, e);
		}
	}

	@Override
	public ParserResult put(UriInfo uriInfo, String parserName, String filePath, InputStream inputStream) {
		return extract(parserName, getQueryParameters(uriInfo), filePath, inputStream);
	}

	private Class<? extends ParserInterface> getClassParserExtension(String extension) {
		return StringUtils.isEmpty(extension) ? null : extractorManager.findParserClassByExtensionFirst(extension);
	}

	private Class<? extends ParserInterface> getClassParserMimeType(String mimeType) {
		return StringUtils.isEmpty(mimeType) ? null : extractorManager.findParserClassByMimeTypeFirst(mimeType);
	}

	private String getMimeMagic(File file) {
		try {
			final MagicMatch match = Magic.getMagicMatch(file, true, true);
			if (match == null)
				return null;
			return match.getMimeType();
		} catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
			return null;
		}
	}

	private ParserResult putMagicPath(UriInfo uriInfo, String filePath, String mimeType) throws Exception {

		final MultivaluedMap<String, String> queryParameters = getQueryParameters(uriInfo);
		final File file = getFilePath(filePath);

		// Find a parser with the extension
		final String extension = FilenameUtils.getExtension(file.getName());
		Class<? extends ParserInterface> parserClass = getClassParserExtension(extension);

		// Find a parser with the mimeType
		if (parserClass == null) {
			if (StringUtils.isEmpty(mimeType))
				mimeType = getMimeMagic(file);
			if (!StringUtils.isEmpty(mimeType))
				parserClass = getClassParserMimeType(mimeType);
		}

		// Do the extraction
		final ParserInterface parser = getParser(parserClass);
		final ParserResultBuilder result = new ParserResultBuilder(parser);
		parser.parseContent(queryParameters, file, extension, mimeType, result);
		return result.build();
	}

	private ParserResult putMagicStream(UriInfo uriInfo, String fileName, String mimeType, InputStream inputStream)
			throws Exception {

		File tempFile = null;
		try {
			Class<? extends ParserInterface> parserClass = null;

			// Find a parser with the extension
			String extension = null;
			if (!StringUtils.isEmpty(fileName)) {
				extension = FilenameUtils.getExtension(fileName);
				parserClass = getClassParserExtension(extension);
			}

			// Find a parser from the mime type
			if (parserClass == null) {
				if (StringUtils.isEmpty(mimeType)) {
					tempFile = File.createTempFile("textextractor",
							extension == null ? StringUtils.EMPTY : "." + extension);
					try {
						IOUtils.copy(inputStream, tempFile);
					} finally {
						IOUtils.closeQuietly(inputStream);
					}
					mimeType = getMimeMagic(tempFile);
				}
				if (!StringUtils.isEmpty(mimeType))
					parserClass = getClassParserMimeType(mimeType);
			}

			// Do the extraction
			final MultivaluedMap<String, String> queryParameters = getQueryParameters(uriInfo);
			final ParserInterface parser = getParser(parserClass);
			final ParserResultBuilder result = new ParserResultBuilder(parser);
			if (tempFile != null)
				parser.parseContent(queryParameters, tempFile, extension, mimeType, result);
			else
				parser.parseContent(queryParameters, inputStream, extension, mimeType, result);
			return result.build();
		} finally {
			if (tempFile != null)
				tempFile.delete();
		}
	}

	@Override
	public ParserResult putMagic(UriInfo uriInfo, String fileName, String filePath, String mimeType,
			InputStream inputStream) {
		try {
			if (checkIsPath(filePath, inputStream))
				return putMagicPath(uriInfo, filePath, mimeType);
			else
				return putMagicStream(uriInfo, fileName, mimeType, inputStream);
		} catch (Exception e) {
			throw ServerException.getJsonException(LOGGER, e);
		}
	}

}
