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
import java.util.List;

import com.qwazr.extractor.ParserAbstract;
import com.qwazr.extractor.ParserDocument;
import com.qwazr.extractor.ParserField;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class Rss extends ParserAbstract {

	public static final String[] DEFAULT_MIMETYPES = { "application/rss+xml" };

	public static final String[] DEFAULT_EXTENSIONS = { "rss" };

	final protected static ParserField CHANNEL_TITLE = ParserField.newString(
			"channel_title", "The title of the channel");

	final protected static ParserField CHANNEL_LINK = ParserField.newString(
			"channel_link", "The link of the channel");

	final protected static ParserField CHANNEL_DESCRIPTION = ParserField
			.newString("channel_description", "The description of the channel");

	final protected static ParserField CHANNEL_CATEGORY = ParserField
			.newString("channel_category", "The category of the channel");

	final protected static ParserField CHANNEL_AUTHOR_NAME = ParserField
			.newString("channel_author_name", "The name of the author");

	final protected static ParserField CHANNEL_AUTHOR_EMAIL = ParserField
			.newString("channel_author_email",
					"The email address of the author");

	final protected static ParserField CHANNEL_CONTRIBUTOR_NAME = ParserField
			.newString("channel_contributor_name",
					"The name of the contributor");

	final protected static ParserField CHANNEL_CONTRIBUTOR_EMAIL = ParserField
			.newString("channel_contributor_email",
					"The email address of the contributor");

	final protected static ParserField CHANNEL_PUBLISHED_DATE = ParserField
			.newString("channel_published_date",
					"The published date of the channel");

	final protected static ParserField ATOM_TITLE = ParserField.newString(
			"atom_title", "The title of the atom");

	final protected static ParserField ATOM_LINK = ParserField.newString(
			"atom_link", "The link of the atom");

	final protected static ParserField ATOM_DESCRIPTION = ParserField
			.newString("atom_description", "The descriptiln of the atom");

	final protected static ParserField ATOM_CATEGORY = ParserField.newString(
			"atom_category", "The category of the atom");

	final protected static ParserField ATOM_AUTHOR_NAME = ParserField
			.newString("atom_author_name", "The name of the author");

	final protected static ParserField ATOM_AUTHOR_EMAIL = ParserField
			.newString("atom_author_email", "The email address of the author");

	final protected static ParserField ATOM_CONTRIBUTOR_NAME = ParserField
			.newString("atom_contributor_name", "The name of the contributor");

	final protected static ParserField ATOM_CONTRIBUTOR_EMAIL = ParserField
			.newString("atom_contributor_email",
					"The email address of the contributor");

	final protected static ParserField ATOM_PUBLISHED_DATE = ParserField
			.newString("atom_published_date", "The published date");

	final protected static ParserField ATOM_UPDATED_DATE = ParserField
			.newString("atom_updated_date", "The updated date");

	final protected static ParserField LANG_DETECTION = ParserField.newString(
			"lang_detection", "Detection of the language");

	final protected static ParserField[] FIELDS = { CHANNEL_TITLE,
			CHANNEL_LINK, CHANNEL_DESCRIPTION, CHANNEL_CATEGORY,
			CHANNEL_AUTHOR_NAME, CHANNEL_AUTHOR_EMAIL,
			CHANNEL_CONTRIBUTOR_NAME, CHANNEL_CONTRIBUTOR_EMAIL,
			CHANNEL_PUBLISHED_DATE, ATOM_TITLE, ATOM_LINK, ATOM_DESCRIPTION,
			ATOM_AUTHOR_NAME, ATOM_AUTHOR_EMAIL, ATOM_CONTRIBUTOR_NAME,
			ATOM_CONTRIBUTOR_EMAIL, ATOM_PUBLISHED_DATE, ATOM_UPDATED_DATE,
			LANG_DETECTION };

	public Rss() {
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

	private void addPersons(ParserField nameField, ParserField emailField,
			List<SyndPerson> persons, ParserDocument parserDocument) {
		if (persons == null)
			return;
		for (SyndPerson person : persons) {
			parserDocument.add(nameField, person.getName());
			parserDocument.add(emailField, person.getEmail());
		}
	}

	private void addLinks(ParserField linkField, List<SyndLink> links,
			ParserDocument parserDocument) {
		if (links == null)
			return;
		for (SyndLink link : links)
			parserDocument.add(linkField, link.getHref());
	}

	private void addCategories(ParserField categoryField,
			List<SyndCategory> categories, ParserDocument parserDocument) {
		if (categories == null)
			return;
		for (SyndCategory category : categories)
			parserDocument.add(categoryField, category.getName());
	}

	@Override
	protected void parseContent(InputStream inputStream, String extension,
			String mimeType) throws Exception {

		SyndFeedInput input = new SyndFeedInput();
		XmlReader reader = new XmlReader(inputStream);
		SyndFeed feed = input.build(reader);
		if (feed == null)
			return;

		metas.add(CHANNEL_TITLE, feed.getTitle());
		metas.add(CHANNEL_DESCRIPTION, feed.getDescription());

		addPersons(CHANNEL_AUTHOR_NAME, CHANNEL_AUTHOR_EMAIL,
				feed.getAuthors(), metas);
		addPersons(CHANNEL_CONTRIBUTOR_NAME, CHANNEL_CONTRIBUTOR_EMAIL,
				feed.getContributors(), metas);
		addLinks(CHANNEL_LINK, feed.getLinks(), metas);
		addCategories(CHANNEL_CATEGORY, feed.getCategories(), metas);

		metas.add(CHANNEL_PUBLISHED_DATE, feed.getPublishedDate());

		List<SyndEntry> entries = feed.getEntries();
		if (entries == null)
			return;

		for (SyndEntry entry : entries) {

			ParserDocument result = getNewParserDocument();

			result.add(ATOM_TITLE, entry.getTitle());
			result.add(ATOM_DESCRIPTION, entry.getDescription());

			addPersons(ATOM_AUTHOR_NAME, ATOM_AUTHOR_EMAIL, entry.getAuthors(),
					result);
			addPersons(ATOM_CONTRIBUTOR_NAME, ATOM_CONTRIBUTOR_EMAIL,
					entry.getContributors(), result);
			addLinks(ATOM_LINK, entry.getLinks(), result);
			addCategories(ATOM_CATEGORY, entry.getCategories(), result);

			result.add(ATOM_PUBLISHED_DATE, entry.getPublishedDate());
			result.add(ATOM_UPDATED_DATE, entry.getUpdatedDate());
			// Apply the language detection
			result.add(LANG_DETECTION,
					languageDetection(ATOM_DESCRIPTION, 10000));
		}

	}

}
