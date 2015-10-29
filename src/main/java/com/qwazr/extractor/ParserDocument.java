/**
 * Copyright 2014 Emmanuel Keller
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
package com.qwazr.extractor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ParserDocument {

	public final LinkedHashMap<String, ArrayList<Object>> fields;

	ParserDocument() {
		fields = new LinkedHashMap<String, ArrayList<Object>>();
	}

	private List<Object> getList(ParserField field, Object value) {
		if (value == null || field == null)
			return null;
		ArrayList<Object> values = fields.get(field.name);
		if (values == null) {
			values = new ArrayList<Object>(1);
			fields.put(field.name, values);
		}
		return values;
	}

	/**
	 * Add a field/value pair to the document
	 *
	 * @param field the name of the field
	 * @param value any value
	 */
	public void add(ParserField field, Object value) {
		List<Object> values = getList(field, value);
		if (values == null)
			return;
		values.add(value);
	}

}
