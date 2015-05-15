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
package com.qwazr.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class ParserResult {

	public String parser_name;

	public long time_elapsed;

	public HashMap<String, List<Object>> metas;

	public final List<HashMap<String, List<Object>>> documents;

	ParserResult() {
		time_elapsed = System.currentTimeMillis();
		documents = null;
		metas = null;
		parser_name = null;
	}

	ParserResult(String parserName, ParserDocument parserMetas,
			List<ParserDocument> parserDocuments) {
		parser_name = parserName;

		// Calculate the time elapsed
		time_elapsed = System.currentTimeMillis() - time_elapsed;

		// Extract the metas
		metas = parserMetas == null ? null : parserMetas.fields;

		documents = new ArrayList<HashMap<String, List<Object>>>(
				parserDocuments == null ? 0 : parserDocuments.size());
		// Extract the documents found
		if (parserDocuments != null)
			for (ParserDocument parserDocument : parserDocuments)
				documents.add(parserDocument.fields);
	}

	/**
	 * @param documentPos
	 *            the position of the document
	 * @param fieldName
	 *            the name of the field
	 * @param valuePos
	 *            the position of the value
	 * @return the value or null
	 */
	@JsonIgnore
	public Object getDocumentFieldValue(int documentPos, String fieldName,
			int valuePos) {
		if (documents == null || documentPos >= documents.size())
			return null;
		List<Object> fieldList = getDocumentFieldValues(documentPos, fieldName);
		if (fieldList.isEmpty() || valuePos >= fieldList.size())
			return null;
		return fieldList.get(valuePos);
	}

	/**
	 * 
	 * @param documentPos
	 *            the position of the document
	 * @param fieldName
	 *            the name of the field
	 * @return a list of values
	 */
	@JsonIgnore
	public List<Object> getDocumentFieldValues(int documentPos, String fieldName) {
		if (documents == null || documentPos >= documents.size())
			return null;
		HashMap<String, List<Object>> fields = documents.get(documentPos);
		List<Object> values = fields.get(fieldName);
		return values == null ? Collections.emptyList() : values;
	}
}
