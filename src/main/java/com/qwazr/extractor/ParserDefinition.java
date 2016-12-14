/**
 * Copyright 2014 Emmanuel Keller
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class ParserDefinition {

	public final ParserField[] returnedFields;

	public final String[] file_extensions;

	public final String[] mime_types;

	public ParserDefinition() {
		returnedFields = null;
		file_extensions = null;
		mime_types = null;
	}

	public ParserDefinition(ParserAbstract parser) {
		ParserField[] parameters = parser.getParameters();
		ParserField[] getParserFields = new ParserField[parameters == null ? 1 : 1 + parameters.length];
		getParserFields[0] = ParserField.newString("path", "path to the local file");
		if (parameters != null)
			System.arraycopy(parameters, 0, getParserFields, 1, parameters.length);
		returnedFields = parser.getFields();
		file_extensions = parser.getDefaultExtensions();
		mime_types = parser.getDefaultMimeTypes();
	}
}
