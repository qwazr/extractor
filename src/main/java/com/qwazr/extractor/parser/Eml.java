/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.util.Properties;

public class Eml extends ParserAbstract {

	static final String[] DEFAULT_MIMETYPES = { "message/rfc822" };

	static final String[] DEFAULT_EXTENSIONS = { "eml" };

	final static ParserField SUBJECT = ParserField.newString("subject", "The subject of the email");

	final static ParserField FROM = ParserField.newString("from", "The from email");

	final static ParserField RECIPIENT_TO = ParserField.newString("recipient_to", "The recipient to");

	final static ParserField RECIPIENT_CC = ParserField.newString("recipient_cc", "The recipient cc");

	final static ParserField RECIPIENT_BCC = ParserField.newString("recipient_bcc", "The recipient bcc");

	final static ParserField SENT_DATE = ParserField.newDate("sent_date", "The sent date");

	final static ParserField RECEIVED_DATE = ParserField.newDate("received_date", "The received date");

	final static ParserField ATTACHMENT_NAME = ParserField.newString("attachment_name", "The attachment name");

	final static ParserField ATTACHMENT_TYPE = ParserField.newString("attachment_type", "The attachment mime type");

	final static ParserField ATTACHMENT_CONTENT = ParserField.newString("attachment_content", "The attachment content");

	final static ParserField PLAIN_CONTENT = ParserField.newString("plain_content", "The plain text body content");

	final static ParserField HTML_CONTENT = ParserField.newString("html_content", "The html text body content");

	final static ParserField LANG_DETECTION = ParserField.newString("lang_detection", "Detection of the language");

	final static ParserField[] FIELDS = { SUBJECT,
			FROM,
			RECIPIENT_TO,
			RECIPIENT_CC,
			RECIPIENT_BCC,
			SENT_DATE,
			RECEIVED_DATE,
			ATTACHMENT_NAME,
			ATTACHMENT_TYPE,
			ATTACHMENT_CONTENT,
			PLAIN_CONTENT,
			HTML_CONTENT,
			LANG_DETECTION };

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

	private final static Properties JAVAMAIL_PROPS = new Properties();

	static {
		JAVAMAIL_PROPS.put("mail.host", "localhost");
		JAVAMAIL_PROPS.put("mail.transport.protocol", "smtp");
	}

	@Override
	public void parseContent(final MultivaluedMap<String, String> parameters, final InputStream inputStream,
			final String extension, final String mimeType, final ParserResultBuilder resultBuilder) throws Exception {
		final Session session = Session.getDefaultInstance(JAVAMAIL_PROPS);

		final MimeMessage mimeMessage = new MimeMessage(session, inputStream);
		final MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage).parse();

		ParserFieldsBuilder document = resultBuilder.newDocument();
		final String from = mimeMessageParser.getFrom();
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
			document.add(LANG_DETECTION, languageDetection(document, PLAIN_CONTENT, 10000));
		else
			document.add(LANG_DETECTION, languageDetection(document, HTML_CONTENT, 10000));
	}

}
