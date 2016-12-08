/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.extractor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ParserResult {

	public String parser_name;

	public Long time_elapsed;

	public LinkedHashMap<String, Object> metas;

	public final ArrayList<LinkedHashMap<String, Object>> documents;

	ParserResult() {
		time_elapsed = null;
		documents = null;
		metas = null;
		parser_name = null;
	}

	ParserResult(final String parserName, long startTime, ParserDocument parserMetas,
			ArrayList<ParserDocument> parserDocuments) {
		parser_name = parserName;

		// Calculate the time elapsed
		time_elapsed = System.currentTimeMillis() - startTime;

		// Extract the metas
		metas = parserMetas == null ? null : parserMetas.fields;

		documents = new ArrayList<>(parserDocuments == null ? 0 : parserDocuments.size());
		// Extract the documents found
		if (parserDocuments != null)
			for (ParserDocument parserDocument : parserDocuments)
				documents.add(parserDocument.fields);
	}

	/**
	 * @param documentPos the position of the document
	 * @param fieldName   the name of the field
	 * @param valuePos    the position of the value
	 * @return the value or null
	 */
	@JsonIgnore
	public Object getDocumentFieldValue(int documentPos, String fieldName, int valuePos) {
		final Object value = getDocumentFieldValues(documentPos, fieldName);
		if (value == null)
			return null;
		if (value instanceof List) {
			List valueList = (List) value;
			if (valuePos >= valueList.size())
				return null;
			return valueList.get(valuePos);
		}
		return valuePos == 0 ? value : null;
	}

	/**
	 * @param documentPos the position of the document
	 * @param fieldName   the name of the field
	 * @return a list of values
	 */
	@JsonIgnore
	public Object getDocumentFieldValues(int documentPos, String fieldName) {
		if (documents == null || documentPos >= documents.size())
			return null;
		LinkedHashMap<String, Object> fields = documents.get(documentPos);
		return fields == null ? null : fields.get(fieldName);
	}
}
