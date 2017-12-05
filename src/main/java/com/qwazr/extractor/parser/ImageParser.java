/*
 * Copyright 2014-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.extractor.parser;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserFieldsBuilder;
import com.qwazr.extractor.ParserResultBuilder;
import com.qwazr.extractor.util.ImagePHash;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class ImageParser extends ParserAbstract {

	private static final String[] DEFAULT_MIMETYPES;

	private static final String[] DEFAULT_EXTENSIONS;

	static {
		DEFAULT_MIMETYPES = ImageIO.getReaderMIMETypes();
		DEFAULT_EXTENSIONS = ImageIO.getReaderFileSuffixes();
	}

	final private static ParserField WIDTH = ParserField.newInteger("width", "Width of the image in pixels");

	final private static ParserField HEIGHT = ParserField.newInteger("height", "Height of the image in pixels");

	final private static ParserField FORMAT = ParserField.newString("format", "The detected format");

	final private static ParserField PHASH = ParserField.newString("phash", "Perceptual Hash");

	final private static ParserField[] FIELDS = { WIDTH, HEIGHT, FORMAT, PHASH };

	@Override
	public ParserField[] getParameters() {
		return null;
	}

	@Override
	public ParserField[] getFields() {
		return FIELDS;
	}

	@Override
	public String[] getDefaultExtensions() {
		return DEFAULT_EXTENSIONS;
	}

	@Override
	public String[] getDefaultMimeTypes() {
		return DEFAULT_MIMETYPES;
	}

	private void browseNodes(String path, final Node root, final ParserFieldsBuilder result) {
		if (root == null)
			return;
		switch (root.getNodeType()) {
		case Node.TEXT_NODE:
			result.add(ParserField.newString(path, null), root.getNodeValue());
			break;
		case Node.ELEMENT_NODE:
			final NamedNodeMap nnm = root.getAttributes();
			if (nnm != null)
				for (int i = 0; i < nnm.getLength(); i++)
					browseNodes(path, nnm.item(i), result);
			Node child = root.getFirstChild();
			while (child != null) {
				browseNodes(path + "/" + child.getNodeName(), child, result);
				child = child.getNextSibling();
			}
			break;
		case Node.ATTRIBUTE_NODE:
			path = path + "#" + root.getNodeName();
			result.add(ParserField.newString(path, null), root.getNodeValue());
			break;
		}
	}

	@Override
	public void parseContent(final MultivaluedMap<String, String> parameters, final Path path, final String extension,
			final String mimeType, final ParserResultBuilder resultBuilder) throws Exception {

		final ImagePHash imgPhash = new ImagePHash();
		try (final ImageInputStream in = ImageIO.createImageInputStream(path.toFile())) {
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				ParserFieldsBuilder result = resultBuilder.newDocument();
				ImageReader reader = readers.next();
				resultBuilder.metas().set(MIME_TYPE, "image/" + reader.getFormatName().toLowerCase());
				try {
					reader.setInput(in);
					result.add(WIDTH, reader.getWidth(0));
					result.add(HEIGHT, reader.getHeight(0));
					result.add(FORMAT, reader.getFormatName());
					result.add(PHASH, imgPhash.getHash(reader.read(0)));
					IIOMetadata metadata = reader.getImageMetadata(0);
					if (metadata != null) {
						String[] names = metadata.getMetadataFormatNames();
						if (names != null)
							for (String name : names)
								browseNodes("META", metadata.getAsTree(name), result);
					}
				} finally {
					reader.dispose();
				}
			}
		}
	}

	@Override
	public void parseContent(final MultivaluedMap<String, String> parameters, final InputStream inputStream,
			final String extension, final String mimeType, final ParserResultBuilder resultBuilder) throws Exception {
		final Path tempFile = ParserAbstract.createTempFile(inputStream, extension == null ? "image" : "." + extension);
		try {
			parseContent(parameters, tempFile, extension, mimeType, resultBuilder);
		} finally {
			Files.deleteIfExists(tempFile);
		}
	}
}
