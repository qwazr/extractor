/**
 * Copyright 2015-2017 Emmanuel Keller
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

final public class ParserFieldsBuilder {

	LinkedHashMap<String, Object> fields;

	private void checkFields() {
		if (fields == null)
			fields = new LinkedHashMap<>();
	}

	private List<Object> getList(final ParserField field, final Object value) {
		if (value == null || field == null)
			return null;
		checkFields();
		List<Object> values = (ArrayList<Object>) fields.get(field.name);
		if (values == null) {
			values = new ArrayList<>(1);
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
	public void add(final ParserField field, final Object value) {
		final List<Object> values = getList(field, value);
		if (values == null)
			return;
		values.add(value);
	}

	/**
	 * Set the unique field/value to the document
	 *
	 * @param field the name of the field
	 * @param value any value
	 */
	public void set(final ParserField field, final Object value) {
		checkFields();
		fields.put(field.name, value);
	}
	
}
