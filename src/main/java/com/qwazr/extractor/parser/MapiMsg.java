/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
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
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hsmf.MAPIMessage;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;

public class MapiMsg extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = { "application/vnd.ms-outlook" };

	public static final String[] DEFAULT_EXTENSIONS = { "msg" };

	final protected static ParserField SUBJECT = ParserField.newString(
			"subject", "The subject of the email");

	final protected static ParserField FROM = ParserField.newString("from",
			"The from email");

	final protected static ParserField RECIPIENT_TO = ParserField.newString(
			"recipient_to", "The recipient to");

	final protected static ParserField RECIPIENT_CC = ParserField.newString(
			"recipient_cc", "The recipient cc");

	final protected static ParserField RECIPIENT_BCC = ParserField.newString(
			"recipient_bcc", "The recipient bcc");

	final protected static ParserField MESSAGE_DATE = ParserField.newDate(
			"message_date", "The message date");

	final protected static ParserField CONVERSATION_TOPIC = ParserField
			.newString("conversation_topic", "The conversation topic");

	final protected static ParserField ATTACHMENT_NAME = ParserField.newString(
			"attachment_name", "The attachment name");

	final protected static ParserField ATTACHMENT_TYPE = ParserField.newString(
			"attachment_type", "The attachment mime type");

	final protected static ParserField ATTACHMENT_CONTENT = ParserField
			.newString("attachment_content", "The attachment content");

	final protected static ParserField PLAIN_CONTENT = ParserField.newString(
			"plain_content", "The plain text body content");

	final protected static ParserField HTML_CONTENT = ParserField.newString(
			"html_content", "The html text body content");

	final protected static ParserField LANG_DETECTION = ParserField.newString(
			"lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS = { SUBJECT, FROM,
			RECIPIENT_TO, RECIPIENT_CC, RECIPIENT_BCC, MESSAGE_DATE,
			CONVERSATION_TOPIC, ATTACHMENT_NAME, ATTACHMENT_TYPE,
			ATTACHMENT_CONTENT, PLAIN_CONTENT, HTML_CONTENT, LANG_DETECTION };

	public MapiMsg() {
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

	private final static Properties JAVAMAIL_PROPS = new Properties();

	static {
		JAVAMAIL_PROPS.put("mail.host", "localhost");
		JAVAMAIL_PROPS.put("mail.transport.protocol", "smtp");
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension,
			String mimeType) throws Exception {
		MAPIMessage msg = new MAPIMessage(inputStream);
		msg.setReturnNullOnMissingChunk(true);

		ParserDocument document = getNewParserDocument();

		document.add(FROM, msg.getDisplayFrom());
		document.add(RECIPIENT_TO, msg.getDisplayTo());
		document.add(RECIPIENT_CC, msg.getDisplayCC());
		document.add(RECIPIENT_BCC, msg.getDisplayBCC());
		document.add(SUBJECT, msg.getSubject());
		document.add(HTML_CONTENT, msg.getHtmlBody());
		document.add(PLAIN_CONTENT, msg.getTextBody());
		document.add(MESSAGE_DATE, msg.getMessageDate());
		document.add(CONVERSATION_TOPIC, msg.getConversationTopic());

		if (StringUtils.isEmpty(msg.getHtmlBody()))
			document.add(LANG_DETECTION,
					languageDetection(document, PLAIN_CONTENT, 10000));
		else
			document.add(LANG_DETECTION,
					languageDetection(document, HTML_CONTENT, 10000));

		// TODO manage attachments
	}

}
