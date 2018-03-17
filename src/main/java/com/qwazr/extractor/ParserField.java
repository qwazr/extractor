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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE,
		fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ParserField implements Serializable {

	/**
	 * The internal name of the field.
	 */
	public final String name;

	/**
	 * The type of the data
	 */
	public final Type type;

	/**
	 * An optional description of the field.
	 */
	public final String description;

	@JsonCreator
	ParserField(@JsonProperty("name") final String name, @JsonProperty("type") final Type type,
			@JsonProperty("description") final String description) {
		this.name = name;
		this.type = type;
		this.description = description;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof ParserField))
			return false;
		if (o == this)
			return true;
		final ParserField f = (ParserField) o;
		return Objects.equals(name, f.name) && Objects.equals(type, f.type) &&
				Objects.equals(description, f.description);
	}

	public static ParserField newString(String name, String description) {
		return new ParserField(name, Type.STRING, description);
	}

	public static ParserField newInteger(String name, String description) {
		return new ParserField(name, Type.INTEGER, description);
	}

	public static ParserField newDate(String name, String description) {
		return new ParserField(name, Type.DATE, description);
	}

	public static ParserField newMap(String name, String description) {
		return new ParserField(name, Type.MAP, description);
	}

	public enum Type {
		STRING, INTEGER, DATE, MAP
	}

}
