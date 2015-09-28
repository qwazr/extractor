/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor.parser;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.DrawingParagraph;
import org.apache.poi.xslf.usermodel.DrawingTextBody;
import org.apache.poi.xslf.usermodel.DrawingTextPlaceholder;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFCommentAuthors;
import org.apache.poi.xslf.usermodel.XSLFComments;
import org.apache.poi.xslf.usermodel.XSLFCommonSlideData;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentAuthor;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;

public class Pptx extends ParserAbstract {

    public static final String[] DEFAULT_MIMETYPES = {
		    "application/vnd.openxmlformats-officedocument.presentationml.presentation" };

    public static final String[] DEFAULT_EXTENSIONS = { "pptx" };

    final protected static ParserField TITLE = ParserField.newString("title", "The title of the document");

    final protected static ParserField CREATOR = ParserField.newString("creator", "The name of the creator");

    final protected static ParserField DESCRIPTION = ParserField.newString("description", null);

    final protected static ParserField KEYWORDS = ParserField.newString("keywords", null);

    final protected static ParserField SUBJECT = ParserField.newString("subject", "The subject of the document");

    final protected static ParserField CREATION_DATE = ParserField.newDate("creation_date", null);

    final protected static ParserField MODIFICATION_DATE = ParserField.newDate("modification_date", null);

    final protected static ParserField SLIDES = ParserField.newString("slides", null);

    final protected static ParserField MASTER = ParserField.newString("master", null);

    final protected static ParserField NOTES = ParserField.newString("notes", null);

    final protected static ParserField COMMENTS = ParserField.newString("comments", null);

    final protected static ParserField LANG_DETECTION = ParserField
		    .newString("lang_detection", "Detection of the language");

    final protected static ParserField[] FIELDS = { TITLE, CREATOR, DESCRIPTION, KEYWORDS, SUBJECT, CREATION_DATE,
		    MODIFICATION_DATE, SLIDES, MASTER, NOTES, COMMENTS, LANG_DETECTION };

    public Pptx() {
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

    @Override
    protected void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception {
	if (StringUtils.isEmpty(extension))
	    extension = ".pptx";
	File tempFile = ParserAbstract.createTempFile(inputStream, extension);
	try {
	    parseContent(tempFile, extension, mimeType);
	} finally {
	    tempFile.delete();
	}
    }

    @Override
    protected void parseContent(File file, String extension, String mimeType) throws Exception {

	XSLFSlideShow pptSlideShow = new XSLFSlideShow(file.getAbsolutePath());
	XMLSlideShow slideshow = new XMLSlideShow(pptSlideShow.getPackage());

	// Extract metadata
	XSLFPowerPointExtractor poiExtractor = null;
	try {
	    poiExtractor = new XSLFPowerPointExtractor(slideshow);
	    CoreProperties info = poiExtractor.getCoreProperties();
	    if (info != null) {
		metas.add(TITLE, info.getTitle());
		metas.add(CREATOR, info.getCreator());
		metas.add(SUBJECT, info.getSubject());
		metas.add(DESCRIPTION, info.getDescription());
		metas.add(KEYWORDS, info.getKeywords());
		metas.add(CREATION_DATE, info.getCreated());
		metas.add(MODIFICATION_DATE, info.getModified());
	    }
	} finally {
	    poiExtractor.close();
	}
	extractSides(slideshow);
    }

    /**
     * Declined from XSLFPowerPointExtractor.java
     */
    private String extractText(XSLFCommonSlideData data, boolean skipPlaceholders) {
	StringBuilder sb = new StringBuilder();
	for (DrawingTextBody textBody : data.getDrawingText()) {
	    if (skipPlaceholders && textBody instanceof DrawingTextPlaceholder) {
		DrawingTextPlaceholder ph = (DrawingTextPlaceholder) textBody;
		if (!ph.isPlaceholderCustom()) {
		    // Skip non-customised placeholder text
		    continue;
		}
	    }

	    for (DrawingParagraph p : textBody.getParagraphs()) {
		sb.append(p.getText());
		sb.append("\n");
	    }
	}
	return sb.toString();
    }

    /**
     * Declined from XSLFPowerPointExtractor.java
     *
     * @param slideshow
     */
    private void extractSides(XMLSlideShow slideshow) {

	List<XSLFSlide> slides = slideshow.getSlides();
	XSLFCommentAuthors commentAuthors = slideshow.getCommentAuthors();

	for (XSLFSlide slide : slides) {

	    // One document per slide
	    ParserDocument result = getNewParserDocument();

	    XSLFNotes notes = slide.getNotes();
	    XSLFComments comments = slide.getComments();
	    XSLFSlideLayout layout = slide.getSlideLayout();
	    XSLFSlideMaster master = layout.getSlideMaster();

	    // TODO Do the slide's name
	    // (Stored in docProps/app.xml)

	    // Do the slide's text
	    result.add(SLIDES, extractText(slide.getCommonSlideData(), false));
	    result.add(LANG_DETECTION, languageDetection(SLIDES, 10000));

	    // If requested, get text from the master and it's layout
	    if (layout != null) {
		result.add(MASTER, extractText(layout.getCommonSlideData(), true));
	    }
	    if (master != null) {
		result.add(MASTER, extractText(master.getCommonSlideData(), true));
	    }

	    // If the slide has comments, do those too
	    if (comments != null) {
		for (CTComment comment : comments.getCTCommentsList().getCmList()) {
		    StringBuilder sbComment = new StringBuilder();
		    // Do the author if we can
		    if (commentAuthors != null) {
			CTCommentAuthor author = commentAuthors.getAuthorById(comment.getAuthorId());
			if (author != null) {
			    sbComment.append(author.getName());
			    sbComment.append(": ");
			}
		    }

		    // Then the comment text, with a new line afterwards
		    sbComment.append(comment.getText());
		    sbComment.append("\n");
		    if (sbComment.length() > 0)
			result.add(COMMENTS, sbComment.toString());
		}
	    }

	    // Do the notes if requested
	    if (notes != null) {
		result.add(NOTES, extractText(notes.getCommonSlideData(), false));
	    }
	}
    }
}
