/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import java.io.InputStream;
import java.util.List;

import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;
import org.apache.poi.hslf.usermodel.HSLFTextRun;

public class Ppt extends ParserAbstract {

    public static final String[] DEFAULT_MIMETYPES = { "application/vnd.ms-powerpoint" };

    public static final String[] DEFAULT_EXTENSIONS = { "ppt" };

    final protected static ParserField TITLE = ParserField.newString("title", "The title of the document");

    final protected static ParserField BODY = ParserField.newString("body", "The body of the document");

    final protected static ParserField NOTES = ParserField.newString("notes", null);

    final protected static ParserField OTHER = ParserField.newString("other", null);

    final protected static ParserField LANG_DETECTION = ParserField
		    .newString("lang_detection", "Detection of the language");

    final protected static ParserField[] FIELDS = { TITLE, BODY, NOTES, OTHER, LANG_DETECTION };

    public Ppt() {
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

	HSLFSlideShow ppt = new HSLFSlideShow(inputStream);

	List<HSLFSlide> slides = ppt.getSlides();
	for (HSLFSlide slide : slides) {
	    ParserDocument document = getNewParserDocument();
	    List<List<HSLFTextParagraph>> textLevel0 = slide.getTextParagraphs();
	    for (List<HSLFTextParagraph> textLevel1 : textLevel0) {
		for (HSLFTextParagraph textPara : textLevel1) {
		    ParserField parserField;
		    switch (textPara.getRunType()) {
		    case TextHeaderAtom.TITLE_TYPE:
		    case TextHeaderAtom.CENTER_TITLE_TYPE:
			parserField = TITLE;
			break;
		    case TextHeaderAtom.NOTES_TYPE:
			parserField = NOTES;
			break;
		    case TextHeaderAtom.BODY_TYPE:
		    case TextHeaderAtom.CENTRE_BODY_TYPE:
		    case TextHeaderAtom.HALF_BODY_TYPE:
		    case TextHeaderAtom.QUARTER_BODY_TYPE:
			parserField = BODY;
			break;
		    case TextHeaderAtom.OTHER_TYPE:
		    default:
			parserField = OTHER;
			break;
		    }
		    StringBuilder sb = new StringBuilder();
		    for (HSLFTextRun textRun : textPara.getTextRuns()) {
			sb.append(textRun.getRawText());
			sb.append(' ');
		    }
		    document.add(parserField, sb.toString().trim());
		}
	    }
	    document.add(LANG_DETECTION, languageDetection(document, BODY, 10000));
	}

    }
}
