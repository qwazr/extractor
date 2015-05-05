/**
 * Copyright 2014-2015 OpenSearchServer Inc.
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
package com.qwazr.extractor.parser;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;

public class Markdown extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = { "text/x-markdown",
			"text/markdown" };

	public static final String[] DEFAULT_EXTENSIONS = { "md", "markdown" };

	final protected static ParserField CONTENT = ParserField.newString(
			"content", "The content of the document");

	final protected static ParserField URL = ParserField.newString("url",
			"Detected URLs");

	final protected static ParserField LANG_DETECTION = ParserField.newString(
			"lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS = { CONTENT, URL,
			LANG_DETECTION };

	private ParserDocument result;

	public Markdown() {
	}

	@Override
	protected ParserField[] getParameters() {
		return null;
	}

	@Override
	protected ParserField[] getFields() {
		return FIELDS;
	}

	@Override
	protected String[] getDefaultExtensions() {
		return DEFAULT_EXTENSIONS;
	}

	@Override
	protected String[] getDefaultMimeTypes() {
		return DEFAULT_MIMETYPES;
	}

	private void parseContent(char[] source) throws Exception {
		// PegDownProcessor is not thread safe One processor per thread
		PegDownProcessor pdp = new PegDownProcessor();
		RootNode rootNode = pdp.parseMarkdown(source);
		result = getNewParserDocument();
		rootNode.accept(new ExtractorSerializer());
		result.add(LANG_DETECTION, languageDetection(CONTENT, 10000));
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension,
			String mimeType) throws Exception {
		parseContent(IOUtils.toCharArray(inputStream));
	}

	public class ExtractorSerializer extends ToHtmlSerializer {

		@Override
		public void visit(RootNode node) {
			super.visit(node);
			nextContent();
		}

		protected void nextContent() {
			if (printer.sb.length() == 0)
				return;
			result.add(CONTENT, printer.sb.toString());
			printer.clear();
		}

		public ExtractorSerializer() {
			super(new LinkRenderer());
		}

		@Override
		public void visit(DefinitionNode node) {
			super.visit(node);
			nextContent();
		}

		@Override
		public void visit(DefinitionTermNode node) {
			super.visit(node);
			nextContent();
		}

		@Override
		public void visit(ParaNode node) {
			super.visit(node);
			nextContent();
		}

		@Override
		public void visit(HeaderNode node) {
			super.visit(node);
			nextContent();
		}

		@Override
		public void visit(ListItemNode node) {
			super.visit(node);
			nextContent();
		}

		@Override
		protected void printTag(TextNode node, String tag) {
			printer.print(node.getText());
		}

		@Override
		protected void printTag(SuperNode node, String tag) {
			visitChildren(node);
		}

		@Override
		protected void printIndentedTag(SuperNode node, String tag) {
			nextContent();
			visitChildren(node);
			nextContent();
		}

		@Override
		protected void printImageTag(LinkRenderer.Rendering rendering) {
			result.add(URL, rendering.href);
			printer.print(rendering.text);
		}

		@Override
		protected void printLink(LinkRenderer.Rendering rendering) {
			result.add(URL, rendering.href);
			printer.print(rendering.text);
		}
	}

}
