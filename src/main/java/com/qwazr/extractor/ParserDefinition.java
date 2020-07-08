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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.Equalizer;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ParserDefinition extends Equalizer.Immutable<ParserDefinition> {

    public final Collection<ParserField> returnedFields;

    public final Collection<String> fileExtensions;

    public final Collection<String> mimeTypes;

    @JsonCreator
    ParserDefinition(final @JsonProperty("returned_fields") Collection<ParserField> returnedFields,
                     final @JsonProperty("file_extensions") Collection<String> fileExtensions,
                     final @JsonProperty("mime_types") Collection<String> mimeTypes) {
        super(ParserDefinition.class);
        this.returnedFields = returnedFields;
        this.fileExtensions = fileExtensions;
        this.mimeTypes = mimeTypes;
    }

    ParserDefinition(final ParserFactory parserFactory) {
        super(ParserDefinition.class);
        returnedFields = parserFactory.getFields();
        fileExtensions = parserFactory.getSupportedFileExtensions();
        mimeTypes = parserFactory.getSupportedMimeTypes().stream().map(MediaType::toString).collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(returnedFields, fileExtensions, mimeTypes);
    }

    @Override
    protected boolean isEqual(final ParserDefinition p) {
        return CollectionsUtils.equals(returnedFields, p.returnedFields)
                && CollectionsUtils.equals(fileExtensions, p.fileExtensions)
                && CollectionsUtils.equals(mimeTypes, p.mimeTypes);
    }
}
