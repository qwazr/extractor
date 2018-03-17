/*
 * Copyright 2015-2017 Emmanuel Keller
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE,
		fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ParserDefinition {

	public final ParserField[] returnedFields;

	public final String[] fileExtensions;

	public final String[] mimeTypes;

	@JsonCreator
	ParserDefinition(@JsonProperty("returned_fields") final ParserField[] returnedFields,
			@JsonProperty("file_extensions") final String[] fileExtensions,
			@JsonProperty("mime_types") final String[] mimeTypes) {
		this.returnedFields = returnedFields;
		this.fileExtensions = fileExtensions;
		this.mimeTypes = mimeTypes;
	}

	ParserDefinition(final ParserInterface parser) {
		final ParserField[] parameters = parser.getParameters();
		final ParserField[] getParserFields = new ParserField[parameters == null ? 1 : 1 + parameters.length];
		getParserFields[0] = ParserField.newString("path", "path to the local file");
		if (parameters != null)
			System.arraycopy(parameters, 0, getParserFields, 1, parameters.length);
		returnedFields = parser.getFields();
		fileExtensions = parser.getDefaultExtensions();
		mimeTypes = parser.getDefaultMimeTypes();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof ParserDefinition))
			return false;
		if (o == this)
			return true;
		final ParserDefinition p = (ParserDefinition) o;
		return Objects.deepEquals(returnedFields, p.returnedFields) &&
				Objects.deepEquals(fileExtensions, p.fileExtensions) && Objects.deepEquals(mimeTypes, p.mimeTypes);
	}
}
