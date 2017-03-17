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
import com.qwazr.extractor.parser.Image;
import com.qwazr.extractor.parser.Odf;
import com.qwazr.extractor.parser.Rtf;
import com.qwazr.extractor.parser.Text;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
		Assert.assertEquals(5, manager.getList().size());
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
	public void testRtf() throws Exception {
		doTest(Rtf.class, "file.rtf", DEFAULT_TEST_STRING);
	}

	@Test
	public void testText() throws Exception {
		doTest(Text.class, "file.txt", DEFAULT_TEST_STRING);
	}

}
