/*
 * Copyright 2014-2017 Emmanuel Keller / QWAZR
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

import java.io.Serializable;

@JsonInclude(Include.NON_EMPTY)
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

	private ParserField(String name, Type type, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
	}

	final public static ParserField newString(String name, String description) {
		return new ParserField(name, Type.STRING, description);
	}

	final public static ParserField newInteger(String name, String description) {
		return new ParserField(name, Type.INTEGER, description);
	}

	final public static ParserField newDate(String name, String description) {
		return new ParserField(name, Type.DATE, description);
	}

	final public static ParserField newMap(String name, String description) {
		return new ParserField(name, Type.MAP, description);
	}

	public enum Type {
		STRING, INTEGER, DATE, MAP
	}

}
