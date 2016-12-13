/**
 * Copyright 2014 Emmanuel Keller
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
package com.qwazr.extractor.test;

import com.qwazr.extractor.ExtractorManager;
import com.qwazr.extractor.ExtractorServiceInterface;
import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserResult;
import com.qwazr.extractor.parser.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AllTest {

	static final Logger LOGGER = LoggerFactory.getLogger(AllTest.class);

	static final String DEFAULT_TEST_STRING = "osstextextractor";

	@BeforeClass
	public static void init() throws IOException {
		ExtractorManager.load(null);
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
	protected ParserAbstract createRegisterInstance(Class<? extends ParserAbstract> className)
			throws InstantiationException, IllegalAccessException, IOException {
		Class<? extends ParserAbstract> parserClass =
				ExtractorManager.getInstance().findParserClassByName(className.getSimpleName().toLowerCase());
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
		Assert.fail("Text " + text + " not found");
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

		File tempFile = getTempFile(fileName);

		// Test stream
		ParserAbstract parser = createRegisterInstance(className);
		ParserResult parserResult = parser.doParsing(uriInfo.getQueryParameters(), getStream(fileName),
				FilenameUtils.getExtension(fileName), null);
		assert (parserResult != null);
		checkContainsText(parserResult, testString);

		// Test file
		parser = createRegisterInstance(className);
		parserResult = parser.doParsing(uriInfo.getQueryParameters(), tempFile, null, null);
		assert (parserResult != null);
		checkContainsText(parserResult, testString);

		// Test stream with magic mime service
		parserResult =
				ExtractorServiceInterface.getClient().putMagic(uriInfo, fileName, null, null, getStream(fileName));
		assert (parserResult != null);
		checkContainsText(parserResult, testString);

		// Test path with magic mime service
		parserResult = ExtractorServiceInterface.getClient()
				.putMagic(uriInfo, fileName, tempFile.getAbsolutePath(), null, null);
		assert (parserResult != null);
		checkContainsText(parserResult, testString);
	}

	final String AUDIO_TEST_STRING = "opensearchserver";

	@Test
	public void testAudioFlag() throws Exception {
		doTest(Audio.class, "file.flac", AUDIO_TEST_STRING, "format", "flac");
	}

	@Test
	public void testAudioM4a() throws Exception {
		doTest(Audio.class, "file.m4a", DEFAULT_TEST_STRING, "format", "m4a");
	}

	@Test
	public void testAudioMp3() throws Exception {
		doTest(Audio.class, "file.mp3", DEFAULT_TEST_STRING, "format", "mp3");
	}

	@Test
	public void testAudioOgg() throws Exception {
		doTest(Audio.class, "file.ogg", AUDIO_TEST_STRING, "format", "ogg");
	}

	@Test
	public void testAudioWav() throws Exception {
		doTest(Audio.class, "file.wav", null, "format", "wav");
	}

	@Test
	public void testAudioWma() throws Exception {
		doTest(Audio.class, "file.wma", AUDIO_TEST_STRING, "format", "wma");
	}

	@Test
	public void testDoc() throws Exception {
		doTest(Doc.class, "file.doc", DEFAULT_TEST_STRING);
	}

	@Test
	public void testDocx() throws Exception {
		doTest(Docx.class, "file.docx", DEFAULT_TEST_STRING);
	}

	@Test
	public void testEml() throws Exception {
		doTest(Eml.class, "file.eml", "Maximum actions in one visit");
	}

	@Test
	public void testHtml() throws Exception {
		doTest(Html.class, "file.html", "search engine software");
	}

	private void testSelector(String[] names, String[] selectors, String param, String[] selectorResults) {
		final ExtractorServiceInterface client = ExtractorServiceInterface.getClient();
		final MultivaluedMap map = new MultivaluedHashMap<>();
		map.addAll(param, selectors);
		if (names != null)
			map.addAll(param + "_name", names);

		ParserResult parserResult = client.extract("html", map, null, getStream("file.html"));
		Assert.assertNotNull(parserResult);
		Map<String, List<String>> results =
				(Map<String, List<String>>) parserResult.getDocumentFieldValue(0, "selectors", 0);
		Assert.assertNotNull(results);
		Assert.assertEquals(selectorResults.length, results.size());
		int i = 0;
		for (String selectorResult : selectorResults) {
			String key = names == null ? Integer.toString(i) : names[i];
			String result = results.get(key).get(0);
			Assert.assertNotNull(result);
			Assert.assertEquals(selectorResult, result);
			i++;
		}
	}

	private final static String[] XPATH_NAMES = { "xp1", "xp2" };
	private final static String[] XPATH_SELECTORS =
			{ "//*[@id=\"crawl\"]/ul/li[1]/strong", "//*[@id=\"download\"]/div/div[2]/div/h3" };
	private final static String[] XPATH_RESULTS = { "web crawler", "Documentation" };

	@Test
	public void testHtmlXPath() {
		testSelector(null, XPATH_SELECTORS, "xpath", XPATH_RESULTS);
		testSelector(XPATH_NAMES, XPATH_SELECTORS, "xpath", XPATH_RESULTS);
	}

	private final static String[] CSS_NAMES = { "css1", "css2" };
	private final static String[] CSS_SELECTORS =
			{ "#crawl > ul > li:nth-child(1) > strong", "#download > div > div:nth-child(2) > div > h3" };
	private final static String[] CSS_RESULTS = { "web crawler", "Documentation" };

	@Test
	public void testHtmlCSS() {
		testSelector(null, CSS_SELECTORS, "css", CSS_RESULTS);
		testSelector(CSS_NAMES, CSS_SELECTORS, "css", CSS_RESULTS);
	}

	@Test
	public void testImageGif() throws Exception {
		doTest(Image.class, "file.gif", DEFAULT_TEST_STRING);
	}

	@Test
	public void testImageJpg() throws Exception {
		doTest(Image.class, "file.jpg", DEFAULT_TEST_STRING);
	}

	@Test
	public void testImagePng() throws Exception {
		doTest(Image.class, "file.png", DEFAULT_TEST_STRING);
	}

	//TODO tiff disabled
	public void testImageTiff() throws Exception {
		doTest(Image.class, "file.tiff", DEFAULT_TEST_STRING);
	}

	@Test
	public void testMarkdown() throws Exception {
		doTest(Markdown.class, "file.md", "extract data to be indexed");
	}

	@Test
	public void testPdf() throws Exception {
		doTest(PdfBox.class, "file.pdf", DEFAULT_TEST_STRING);
	}

	@Test
	public void testOcr() throws Exception {
		try {
			doTest(Ocr.class, "file.pdf", DEFAULT_TEST_STRING);
		} catch (UnsatisfiedLinkError e) {
			LOGGER.warn("OCR skipped: no TESTDATA_PREFIX", e);
		}
	}

	@Test
	public void testPwdPdf() throws Exception {
		doTest(PdfBox.class, "file-pass.pdf", DEFAULT_TEST_STRING, "password", "1234");
	}

	@Test
	public void testOdt() throws Exception {
		doTest(Odf.class, "file.odt", DEFAULT_TEST_STRING);
	}

	@Test
	public void testOds() throws Exception {
		doTest(Odf.class, "file.ods", DEFAULT_TEST_STRING);
	}

	@Test
	public void testOdp() throws Exception {
		doTest(Odf.class, "file.odp", DEFAULT_TEST_STRING);
	}

	@Test
	public void testPpt() throws Exception {
		doTest(Ppt.class, "file.ppt", DEFAULT_TEST_STRING);
	}

	@Test
	public void testPptx() throws Exception {
		doTest(Pptx.class, "file.pptx", DEFAULT_TEST_STRING);
	}

	@Test
	public void testRss() throws Exception {
		doTest(Rss.class, "file.rss", "oss-text-extractor");
	}

	@Test
	public void testRtf() throws Exception {
		doTest(Rtf.class, "file.rtf", DEFAULT_TEST_STRING);
	}

	@Test
	public void testText() throws Exception {
		doTest(Text.class, "file.txt", DEFAULT_TEST_STRING);
	}

	@Test
	public void testXls() throws Exception {
		doTest(Xls.class, "file.xls", DEFAULT_TEST_STRING);
	}

	@Test
	public void testXlsx() throws Exception {
		doTest(Xlsx.class, "file.xlsx", DEFAULT_TEST_STRING);
	}

}
