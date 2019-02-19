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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExtractorServerTest {

	@Before
	public void setup() throws Exception {
		ExtractorServer.main();
	}

	@Test
	public void serverTest() {
		Assert.assertNotNull(ExtractorServer.getInstance());
	}

	@After
	public void cleanup() {
		ExtractorServer.shutdown();
	}
}