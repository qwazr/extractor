/*
 * Copyright 2015-2020 Emmanuel Keller
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.ws.rs.core.MediaType;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Assert;
import org.junit.Test;

public class ShadedJarTest {

    @Test
    public void test() throws IOException {
        try (final ExtractorManager extractorManager = new ExtractorManager()) {
            extractorManager.registerShadedJars(Path.of("src", "test", "uber-jar"));
            try {
                Class.forName("com.qwazr.library.markdown.MarkdownParser");
                Assert.fail("Exception not thrown");
            } catch (ClassNotFoundException e) {
                //
            }
            // Check Markdown stream
            final String markdown = "## Test ";
            try (final ByteArrayInputStream input = new ByteArrayInputStream(markdown.getBytes(StandardCharsets.UTF_8))) {
                final ParserResult parserResult = extractorManager.extract(null, input, MediaType.valueOf("text/markdown"));
                assertThat(parserResult, notNullValue());
                final Object object = parserResult.getDocumentFieldValue(0, "content", 0);
                assertThat(object, equalTo("Test"));
            }
            // Check Docx file
            final ParserResult parserResult = extractorManager.extract(null,
                    Path.of("src", "test", "resources", "com", "qwazr", "extractor", "file.docx"));
            assertThat(parserResult, notNullValue());
            final Object object = parserResult.getDocumentFieldValue(0, "content", 0);
            assertThat(object.toString(), containsString("docx parser"));
        }
    }
}
