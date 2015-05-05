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
import java.util.Properties;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;

public class Eml extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = { "message/rfc822" };

	public static final String[] DEFAULT_EXTENSIONS = { "eml" };

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

	final protected static ParserField SENT_DATE = ParserField.newDate(
			"sent_date", "The sent date");

	final protected static ParserField RECEIVED_DATE = ParserField.newDate(
			"received_date", "The received date");

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
			RECIPIENT_TO, RECIPIENT_CC, RECIPIENT_BCC, SENT_DATE,
			RECEIVED_DATE, ATTACHMENT_NAME, ATTACHMENT_TYPE,
			ATTACHMENT_CONTENT, PLAIN_CONTENT, HTML_CONTENT, LANG_DETECTION };

	public Eml() {
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
		Session session = Session.getDefaultInstance(JAVAMAIL_PROPS);

		MimeMessage mimeMessage = new MimeMessage(session, inputStream);
		MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage)
				.parse();

		ParserDocument document = getNewParserDocument();
		String from = mimeMessageParser.getFrom();
		if (from != null)
			document.add(FROM, from.toString());
		for (Address address : mimeMessageParser.getTo())
			document.add(RECIPIENT_TO, address.toString());
		for (Address address : mimeMessageParser.getCc())
			document.add(RECIPIENT_CC, address.toString());
		for (Address address : mimeMessageParser.getBcc())
			document.add(RECIPIENT_BCC, address.toString());
		document.add(SUBJECT, mimeMessageParser.getSubject());
		document.add(HTML_CONTENT, mimeMessageParser.getHtmlContent());
		document.add(PLAIN_CONTENT, mimeMessageParser.getPlainContent());
		document.add(SENT_DATE, mimeMessage.getSentDate());
		document.add(RECEIVED_DATE, mimeMessage.getReceivedDate());

		for (DataSource dataSource : mimeMessageParser.getAttachmentList()) {
			document.add(ATTACHMENT_NAME, dataSource.getName());
			document.add(ATTACHMENT_TYPE, dataSource.getContentType());
			// TODO Extract content from attachmend
			// if (parserSelector != null) {
			// Parser attachParser = parserSelector.parseStream(
			// getSourceDocument(), dataSource.getName(),
			// dataSource.getContentType(), null,
			// dataSource.getInputStream(), null, null, null);
			// if (attachParser != null) {
			// List<ParserResultItem> parserResults = attachParser
			// .getParserResults();
			// if (parserResults != null)
			// for (ParserResultItem parserResult : parserResults)
			// result.addField(
			// ParserFieldEnum.email_attachment_content,
			// parserResult);
			// }
			// }
		}
		if (StringUtils.isEmpty(mimeMessageParser.getHtmlContent()))
			document.add(LANG_DETECTION,
					languageDetection(document, PLAIN_CONTENT, 10000));
		else
			document.add(LANG_DETECTION,
					languageDetection(document, HTML_CONTENT, 10000));
	}

}
