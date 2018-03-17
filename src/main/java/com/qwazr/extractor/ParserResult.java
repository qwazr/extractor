/*
 * Copyright 2014-2018 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE,
		fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ParserResult {

	@JsonProperty("parser_name")
	final public String parserName;

	@JsonProperty("time_elapsed")
	final public Long timeElapsed;

	final public LinkedHashMap<String, Object> metas;

	final public List<LinkedHashMap<String, Object>> documents;

	@JsonCreator
	ParserResult(@JsonProperty("parser_name") final String parserName,
			@JsonProperty("time_elapsed") final Long timeElapsed,
			@JsonProperty("metas") LinkedHashMap<String, Object> metas,
			@JsonProperty("documents") List<LinkedHashMap<String, Object>> documents) {
		this.parserName = parserName;
		this.timeElapsed = timeElapsed;
		this.metas = metas;
		this.documents = documents;
	}

	ParserResult(final ParserResultBuilder builder) {
		parserName = builder.parserName;

		// Calculate the time elapsed
		timeElapsed = System.currentTimeMillis() - builder.startTime;

		// Extract the metas
		metas = builder.metasBuilder == null ? null : builder.metasBuilder.fields;

		// Extract the documents found
		if (builder.documentsBuilders != null && !builder.documentsBuilders.isEmpty()) {
			documents = new ArrayList<>(builder.documentsBuilders.size());
			builder.documentsBuilders.forEach(doc -> documents.add(doc.fields));
		} else
			documents = Collections.emptyList();

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
		final LinkedHashMap<String, Object> fields = documents.get(documentPos);
		return fields == null ? null : fields.get(fieldName);
	}
}
