/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor;

import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.server.ServerException;
import net.sf.jmimemagic.*;
import org.apache.commons.io.FilenameUtils;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ExtractorServiceImpl implements ExtractorServiceInterface {

	@Override
	public Map<String, ResourceLink> list() {
		Set<String> parserList = ParserManager.INSTANCE.getList();
		Map<String, ResourceLink> map = new LinkedHashMap<String, ResourceLink>(
				parserList.size());
		for (String parserName : parserList)
			map.put(parserName, new ResourceLink(
					ClusterManager.INSTANCE.myAddress + "/extractor/"
							+ parserName));
		return map;
	}

	private ParserAbstract getParser(Class<? extends ParserAbstract> parserClass)
			throws ServerException {
		try {
			if (parserClass == null)
				throw new ServerException(Status.NOT_FOUND, "No parser found.");
			return parserClass.newInstance();
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | SecurityException e) {
			throw new ServerException(e);
		}
	}

	private ParserAbstract getParser(String parserName) throws ServerException {
		Class<? extends ParserAbstract> parserClass = ParserManager.INSTANCE
				.findParserClassByName(parserName);
		if (parserClass == null)
			throw new ServerException(Status.NOT_FOUND, "Unknown parser: "
					+ parserName);
		return getParser(parserClass);
	}

	private File getFilePath(String path) throws ServerException {
		File file = new File(path);
		if (!file.exists())
			throw new ServerException(Status.NOT_FOUND, "File not found: "
					+ path);
		if (!file.isFile())
			throw new ServerException(Status.NOT_ACCEPTABLE,
					"The path is not a file: " + path);
		return file;
	}

	@Override
	public Object get(UriInfo uriInfo, String parserName, String path) {
		try {
			ParserAbstract parser = getParser(parserName);
			if (path == null)
				return new ParserDefinition(uriInfo.getPath(), parser);
			File file = getFilePath(path);
			return parser.doParsing(getQueryParameters(uriInfo), file, null,
					null);
		} catch (Exception e) {
			throw ServerException.getJsonException(e);
		}
	}

	private boolean checkIsPath(String filePath, InputStream inputStream)
			throws ServerException {
		boolean isPath = !StringUtils.isEmpty(filePath);
		if (!isPath && inputStream == null)
			throw new ServerException(Status.NOT_ACCEPTABLE,
					"Not path and no stream.");
		return isPath;
	}

	private MultivaluedMap<String, String> getQueryParameters(UriInfo uriInfo) {
		return uriInfo == null ? null : uriInfo.getQueryParameters();
	}

	@Override
	public ParserResult extract(String parserName,
								MultivaluedMap<String, String> parameters, String filePath,
								InputStream inputStream) {
		try {
			ParserAbstract parser = getParser(parserName);

			if (checkIsPath(filePath, inputStream))
				return parser.doParsing(parameters, getFilePath(filePath),
						null, null);
			else
				return parser.doParsing(parameters, inputStream, null, null);
		} catch (Exception e) {
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public ParserResult put(UriInfo uriInfo, String parserName,
							String filePath, InputStream inputStream) {
		return extract(parserName, getQueryParameters(uriInfo), filePath,
				inputStream);
	}

	private Class<? extends ParserAbstract> getClassParserExtension(
			String extension) {
		if (StringUtils.isEmpty(extension))
			return null;
		return ParserManager.INSTANCE
				.findParserClassByExtensionFirst(extension);
	}

	private Class<? extends ParserAbstract> getClassParserMimeType(
			String mimeType) {
		if (StringUtils.isEmpty(mimeType))
			return null;
		return ParserManager.INSTANCE.findParserClassByMimeTypeFirst(mimeType);
	}

	private String getMimeMagic(File file) {
		try {
			MagicMatch match = Magic.getMagicMatch(file, true, true);
			if (match == null)
				return null;
			return match.getMimeType();
		} catch (MagicParseException | MagicMatchNotFoundException
				| MagicException e) {
			return null;
		}
	}

	private ParserResult putMagicPath(UriInfo uriInfo, String filePath,
									  String mimeType) throws Exception {

		MultivaluedMap<String, String> queryParameters = getQueryParameters(uriInfo);
		File file = getFilePath(filePath);

		// Find a parser with the extension
		String extension = FilenameUtils.getExtension(file.getName());
		Class<? extends ParserAbstract> parserClass = getClassParserExtension(extension);

		// Find a parser with the mimeType
		if (parserClass == null) {
			if (StringUtils.isEmpty(mimeType))
				mimeType = getMimeMagic(file);
			if (!StringUtils.isEmpty(mimeType))
				parserClass = getClassParserMimeType(mimeType);
		}

		// Do the extraction
		return getParser(parserClass).doParsing(queryParameters, file,
				extension, mimeType);
	}

	private ParserResult putMagicStream(UriInfo uriInfo, String fileName,
										String mimeType, InputStream inputStream) throws Exception {

		File tempFile = null;
		try {
			Class<? extends ParserAbstract> parserClass = null;

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
							extension == null ? StringUtils.EMPTY : "."
									+ extension);
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
			MultivaluedMap<String, String> queryParameters = getQueryParameters(uriInfo);
			ParserAbstract parser = getParser(parserClass);
			ParserResult result;
			if (tempFile != null)
				result = parser.doParsing(queryParameters, tempFile, extension,
						mimeType);
			else
				result = parser.doParsing(queryParameters, inputStream,
						extension, mimeType);
			return result;
		} finally {
			if (tempFile != null)
				tempFile.delete();
		}
	}

	@Override
	public ParserResult putMagic(UriInfo uriInfo, String fileName,
								 String filePath, String mimeType, InputStream inputStream) {
		try {
			if (checkIsPath(filePath, inputStream))
				return putMagicPath(uriInfo, filePath, mimeType);
			else
				return putMagicStream(uriInfo, fileName, mimeType, inputStream);
		} catch (Exception e) {
			throw ServerException.getJsonException(e);
		}
	}

}
