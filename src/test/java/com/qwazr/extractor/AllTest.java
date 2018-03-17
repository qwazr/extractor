/*
 * Copyright 2015-2018 Emmanuel Keller
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

import com.qwazr.extractor.parser.ImageParser;
import com.qwazr.extractor.parser.RtfParser;
import com.qwazr.extractor.parser.TextParser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AllTest extends ParserTest {

	static final String DEFAULT_TEST_STRING = "osstextextractor";

	static ExtractorManager manager;

	@BeforeClass
	public static void init() {
		manager = new ExtractorManager();
		manager.registerServices();
	}

	public AllTest() {
		super(manager);
	}

	@Test
	public void numberOfParsers() {
		Assert.assertFalse(manager.getList().isEmpty());
	}

	@Test
	public void testImageGif() throws Exception {
		doTest(ImageParser.class, "file.gif", "image/gif", null, DEFAULT_TEST_STRING);
	}

	@Test
	public void testImageJpg() throws Exception {
		doTest(ImageParser.class, "file.jpg", "image/jpeg", null, DEFAULT_TEST_STRING);
	}

	@Test
	public void testImagePng() throws Exception {
		doTest(ImageParser.class, "file.png", "image/png", null, DEFAULT_TEST_STRING);
	}

	//TODO tiff disabled
	public void testImageTiff() throws Exception {
		doTest(ImageParser.class, "file.tiff", "image/tif", null, DEFAULT_TEST_STRING);
	}

	@Test
	public void testRtf() throws Exception {
		doTest(RtfParser.class, "file.rtf", "application/rtf", "content", DEFAULT_TEST_STRING);
	}

	@Test
	public void testText() throws Exception {
		doTest(TextParser.class, "file.txt", "text/plain", "content", DEFAULT_TEST_STRING);
	}

}
