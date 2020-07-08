/*
 * Copyright 2014-2020 Emmanuel Keller / QWAZR
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

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
    ParserResult(final @JsonProperty("parser_name") String parserName,
                 final @JsonProperty("time_elapsed") Long timeElapsed,
                 final @JsonProperty("metas") LinkedHashMap<String, Object> metas,
                 final @JsonProperty("documents") List<LinkedHashMap<String, Object>> documents) {
        this.parserName = parserName;
        this.timeElapsed = timeElapsed;
        this.metas = metas;
        this.documents = documents;
    }

    ParserResult(final Builder builder) {
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
            final List<?> valueList = (List<?>) value;
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

    public static Builder of(String parserName) {
        return new Builder(parserName);
    }

    final static public class Builder {

        private final String parserName;

        private final long startTime;

        private FieldsBuilder metasBuilder;

        private List<FieldsBuilder> documentsBuilders;

        private Builder(final String parserName) {
            this.parserName = parserName;
            this.startTime = System.currentTimeMillis();
        }

        public FieldsBuilder newDocument() {
            if (documentsBuilders == null)
                documentsBuilders = new ArrayList<>();
            final FieldsBuilder parserDocumentBuilder = new FieldsBuilder();
            documentsBuilders.add(parserDocumentBuilder);
            return parserDocumentBuilder;
        }

        public FieldsBuilder metas() {
            if (metasBuilder == null)
                metasBuilder = new FieldsBuilder();
            return metasBuilder;
        }

        public void forEachDocument(Consumer<FieldsBuilder> documentConsumer) {
            if (documentsBuilders != null)
                documentsBuilders.forEach(documentConsumer);
        }

        public ParserResult build() {
            return new ParserResult(this);
        }
    }

    final static public class FieldsBuilder {

        private LinkedHashMap<String, Object> fields;

        /**
         * Add a field/value pair to the document
         *
         * @param field the name of the field
         * @param value any value
         */
        public void add(final ParserField field, final Object value) {
            if (value == null)
                return;
            if (fields == null)
                fields = new LinkedHashMap<>();
            final List<Object> values = (List<Object>) fields.computeIfAbsent(field.name, f -> new ArrayList<Object>(1));
            if (value instanceof Collection)
                values.addAll((Collection) value);
            else
                values.add(value);
        }

        /**
         * Set the unique field/value to the document
         *
         * @param field the name of the field
         * @param value any value
         */
        public void set(final ParserField field, final Object value) {
            if (value == null)
                return;
            if (fields == null)
                fields = new LinkedHashMap<>();
            fields.put(field.name, value);
        }

        public void extractField(final ParserField source,
                                 final int maxLength,
                                 final StringBuilder sb) {
            if (sb.length() >= maxLength)
                return;
            final Object value = fields.get(source.name);
            if (value == null)
                return;
            if (value instanceof List) {
                for (Object object : (List<?>) value) {
                    if (object == null)
                        continue;
                    sb.append(object.toString());
                    sb.append(' ');
                    if (sb.length() >= maxLength)
                        return;
                }
            } else
                sb.append(value.toString());
        }

    }
}
