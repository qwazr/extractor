/**
 * Copyright 2015-2017 Emmanuel Keller
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

import com.qwazr.utils.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public class ParserTest {

	static final Logger LOGGER = LoggerFactory.getLogger(ParserTest.class);

	protected final ExtractorManager manager;
	protected final ExtractorServiceInterface service;

	public ParserTest(final ExtractorManager manager) {
		this.manager = manager;
		this.service = manager.getService();
	}

	/**
	 * Check if the parser has been registered, and create the an instance.
	 *
	 * @param className
	 * @return An instance
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 */
	protected ParserInterface createRegisterInstance(Class<? extends ParserAbstract> className)
			throws InstantiationException, IllegalAccessException, IOException {
		Class<? extends ParserInterface> parserClass =
				manager.findParserClassByName(className.getSimpleName().toLowerCase());
		assert (parserClass != null);
		return parserClass.newInstance();
	}

	protected InputStream getStream(String fileName) {
		InputStream inputStream = getClass().getResourceAsStream(fileName);
		assert (inputStream != null);
		return inputStream;
	}

	protected File getTempFile(String fileName) throws IOException {
		File tempFile = File.createTempFile("oss_extractor", "." + FilenameUtils.getExtension(fileName));
		FileOutputStream fos = new FileOutputStream(tempFile);
		InputStream inputStream = getStream(fileName);
		IOUtils.copy(inputStream, fos);
		return tempFile;
	}

	/**
	 * Check if the given string is present in a map
	 *
	 * @param map
	 * @param text
	 * @return
	 */
	protected boolean checkMapContainsText(Map<String, Object> map, String text) {
		for (Object value : map.values())
			if (checkContainsText(value, text))
				return true;
		return false;
	}

	protected boolean checkCollectionContainsText(Collection<Object> collection, String text) {
		for (Object value : collection)
			if (checkContainsText(value, text))
				return true;
		return false;
	}

	protected boolean checkContainsText(Object value, String text) {
		if (value == null)
			return false;
		if (value instanceof Collection)
			return checkCollectionContainsText((Collection) value, text);
		if (value instanceof Map)
			return checkMapContainsText((Map) value, text);
		return value.toString().contains(text);
	}

	/**
	 * Check if the given string is present in the result
	 *
	 * @param result
	 * @param text
	 */
	protected void checkContainsText(ParserResult result, String text) {
		if (text == null)
			return;
		if (checkContainsText(result.documents, text))
			return;
		if (checkContainsText(result.metas, text))
			return;
		assert false;
	}

	/**
	 * Test inputstream and file parsing
	 *
	 * @param className
	 * @param fileName
	 * @param keyValueParams
	 * @throws Exception
	 */
	protected void doTest(Class<? extends ParserAbstract> className, String fileName, String testString,
			String... keyValueParams) throws Exception {
		LOGGER.info("Testing " + className);

		final UriInfo uriInfo = new UriInfoMock(keyValueParams);

		// Test service name
		assert service.list().contains(StringUtils.removeEnd(className.getSimpleName(), "Parser").toLowerCase());

		File tempFile = getTempFile(fileName);

		// Test stream
		ParserInterface parser = createRegisterInstance(className);
		ParserResultBuilder resultBuilder = new ParserResultBuilder(parser);
		parser.parseContent(uriInfo.getQueryParameters(), getStream(fileName), FilenameUtils.getExtension(fileName),
				null, resultBuilder);
		ParserResult parserResult = resultBuilder.build();
		assert (parserResult != null);
		checkContainsText(parserResult, testString);

		// Test file
		parser = createRegisterInstance(className);
		resultBuilder = new ParserResultBuilder(parser);
		parser.parseContent(uriInfo.getQueryParameters(), tempFile, null, null, resultBuilder);
		assert (parserResult != null);
		checkContainsText(parserResult, testString);

		// No magic to test if the parser doesn't support detection
		if (parser.getDefaultMimeTypes() == null && parser.getDefaultExtensions() == null)
			return;

		// Test stream with magic mime service
		parserResult = service.putMagic(uriInfo, fileName, null, null, getStream(fileName));
		assert (parserResult != null);
		checkContainsText(parserResult, testString);

		// Test path with magic mime service
		parserResult = service.putMagic(uriInfo, fileName, tempFile.getAbsolutePath(), null, null);
		assert (parserResult != null);
		checkContainsText(parserResult, testString);
	}

}
