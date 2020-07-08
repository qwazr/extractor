/*
 * Copyright 2014-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.extractor.ParserFactory;
import com.qwazr.extractor.ParserField;
import com.qwazr.extractor.ParserInterface;
import com.qwazr.extractor.ParserResult;
import com.qwazr.extractor.ParserUtils;
import com.qwazr.extractor.util.ImagePHash;
import com.qwazr.utils.AutoCloseWrapper;
import com.qwazr.utils.LoggerUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.NotImplementedException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ImageParser implements ParserFactory, ParserInterface {

    private final static Logger LOGGER = LoggerUtils.getLogger(ImageParser.class);

    private static final String NAME = "Image";

    private static final List<MediaType> DEFAULT_MIMETYPES;

    private static final List<String> DEFAULT_EXTENSIONS;

    static {
        DEFAULT_EXTENSIONS = Arrays.asList(ImageIO.getReaderFileSuffixes());

        final List<MediaType> types = new ArrayList<>();
        for (final String type : ImageIO.getReaderMIMETypes())
            types.add(MediaType.valueOf(type));
        DEFAULT_MIMETYPES = List.copyOf(types);
    }

    final private static ParserField WIDTH = ParserField.newInteger("width", "Width of the image in pixels");

    final private static ParserField HEIGHT = ParserField.newInteger("height", "Height of the image in pixels");

    final private static ParserField FORMAT = ParserField.newString("format", "The detected format");

    final private static ParserField PHASH = ParserField.newString("phash", "Perceptual Hash");

    final private static List<ParserField> FIELDS = List.of(WIDTH, HEIGHT, FORMAT, PHASH);

    @Override
    public List<ParserField> getParameters() {
        return null;
    }

    @Override
    public List<ParserField> getFields() {
        return FIELDS;
    }

    @Override
    public List<String> getSupportedFileExtensions() {
        return DEFAULT_EXTENSIONS;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ParserInterface createParser() {
        return this;
    }

    @Override
    public List<MediaType> getSupportedMimeTypes() {
        return DEFAULT_MIMETYPES;
    }

    private void browseNodes(String path, final Node root, final ParserResult.FieldsBuilder result) {
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
            default:
                throw new NotImplementedException("Unknown attribute: " + root.getNodeType());
        }
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters, final Path path) throws IOException {
        final ParserResult.Builder resultBuilder = ParserResult.of(NAME);
        final ImagePHash imgPhash = new ImagePHash();
        try (final ImageInputStream in = ImageIO.createImageInputStream(path.toFile())) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ParserResult.FieldsBuilder result = resultBuilder.newDocument();
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
        return resultBuilder.build();
    }

    @Override
    public ParserResult extract(final MultivaluedMap<String, String> parameters,
                                final InputStream inputStream,
                                final MediaType mediaType) throws IOException {
        try (final AutoCloseWrapper<Path> a = AutoCloseWrapper.of(
                ParserUtils.createTempFile(inputStream, "image" + "." + mediaType.getSubtype()),
                LOGGER, Files::deleteIfExists)) {
            return extract(parameters, a.get());
        }
    }


}
