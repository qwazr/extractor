/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.util.ImagePHash;

public class Image extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = ImageIO
			.getReaderMIMETypes();

	public static final String[] DEFAULT_EXTENSIONS = ImageIO
			.getReaderFileSuffixes();

	final protected static ParserField WIDTH = ParserField.newInteger("width",
			"Width of the image in pixels");

	final protected static ParserField HEIGHT = ParserField.newInteger(
			"height", "Height of the image in pixels");

	final protected static ParserField FORMAT = ParserField.newString("format",
			"The detected format");

	final protected static ParserField PHASH = ParserField.newString("phash",
			"Perceptual Hash");

	final protected static ParserField[] FIELDS = { WIDTH, HEIGHT, FORMAT,
			PHASH };

	public Image() {
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

	private void browseNodes(String path, Node root, ParserDocument result) {
		if (root == null)
			return;
		switch (root.getNodeType()) {
		case Node.TEXT_NODE:
			result.add(ParserField.newString(path, null), root.getNodeValue());
			break;
		case Node.ELEMENT_NODE:
			NamedNodeMap nnm = root.getAttributes();
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
	protected void parseContent(File file, String extension, String mimeType)
			throws Exception {
		ImagePHash imgPhash = new ImagePHash();
		ImageInputStream in = ImageIO.createImageInputStream(file);
		try {
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				ParserDocument result = getNewParserDocument();
				ImageReader reader = readers.next();
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
								browseNodes("META", metadata.getAsTree(name),
										result);
					}
				} finally {
					reader.dispose();
				}
			}
		} finally {
			if (in != null)
				in.close();
		}
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension,
			String mimeType) throws Exception {
		File tempFile = ParserAbstract.createTempFile(inputStream,
				extension == null ? "image" : "." + extension);
		try {
			parseContent(tempFile, extension, mimeType);
		} finally {
			tempFile.delete();
		}
	}
}
