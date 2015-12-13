/**
 * Copyright 2014 Emmanuel Keller
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class Link {

	public final Method method;
	public final String rel;
	public final String href;
	public final ParserField[] queryString;

	public Link(Method method, String rel, String href,
			ParserField... parameters) {
		this.method = method;
		this.rel = rel;
		this.href = href;
		this.queryString = parameters;
	}

	public enum Method {
		GET, POST, PUT, DELETE, PATCH, OPTIONS
	}

}
