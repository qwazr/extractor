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

import com.qwazr.extractor.parser.Audio;
import com.qwazr.extractor.parser.Html;
import com.qwazr.extractor.parser.Image;
import com.qwazr.extractor.parser.Odf;
import com.qwazr.extractor.parser.Rss;
import com.qwazr.extractor.parser.Rtf;
import com.qwazr.extractor.parser.Text;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AllTest extends ParserTest {

	static final Logger LOGGER = LoggerFactory.getLogger(AllTest.class);

	static final String DEFAULT_TEST_STRING = "osstextextractor";

	static ExtractorManager manager;

	@BeforeClass
	public static void init() throws IOException, ClassNotFoundException {
		manager = new ExtractorManager(null);
		manager.registerByJsonResources();
	}

	public AllTest() {
		super(manager);
	}

	final String AUDIO_TEST_STRING = "opensearchserver";

	@Test
	public void numberOfParsers() {
		Assert.assertEquals(7, manager.getList().size());
	}

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
	public void testHtml() throws Exception {
		doTest(Html.class, "file.html", "search engine software");
	}

	private void testSelector(String[] names, String[] selectors, String param, String[] selectorResults) {
		final MultivaluedMap map = new MultivaluedHashMap<>();
		map.addAll(param, selectors);
		if (names != null)
			map.addAll(param + "_name", names);

		ParserResult parserResult = service.extract("html", map, null, getStream("file.html"));
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

}
