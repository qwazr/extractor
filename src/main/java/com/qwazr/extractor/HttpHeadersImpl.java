/*
 * Copyright 2015-2020 Emmanuel Keller
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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Partial immplementation for tests
 */
class HttpHeadersImpl implements HttpHeaders {

    private final MultivaluedHashMap<String, String> headers;

    HttpHeadersImpl(final Map<String, String> headers) {
        this.headers = new MultivaluedHashMap<>();
        headers.forEach(this.headers::add);
    }

    @Override
    public List<String> getRequestHeader(String name) {
        return headers.get(name);
    }

    @Override
    public String getHeaderString(String name) {
        return headers.getFirst(name);
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return headers;
    }

    private <T> T getFirst(String name, Function<String, T> function, T defaultValue) {
        final String value = headers.getFirst(name);
        return value == null ? defaultValue : function.apply(value);
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return null;
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return null;
    }

    @Override
    public MediaType getMediaType() {
        return getFirst(HttpHeaders.CONTENT_TYPE, h -> MediaType.valueOf(h), null);
    }

    @Override
    public Locale getLanguage() {
        return null;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return null;
    }

    @Override
    public Date getDate() {
        return null;
    }

    @Override
    public int getLength() {
        return 0;
    }

}
