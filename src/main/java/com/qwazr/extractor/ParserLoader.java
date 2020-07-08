/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.concurrent.FunctionEx;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.ws.rs.InternalServerErrorException;

class ParserLoader implements AutoCloseable {

    private final URLClassLoader classLoader;

    ParserLoader(final Path classesPath, final Path libPath) throws IOException {

        final List<URL> urls = new ArrayList<>();
        if (classesPath != null)
            urls.add(toUrl(classesPath));
        if (libPath != null) {
            try (final Stream<Path> files = Files.list(libPath)) {
                files.filter(p -> Files.isRegularFile(p) && p.endsWith(".jar")).forEach(p -> urls.add(toUrl(p)));
            }
        }

        // feed your URLs to a URLClassLoader!
        classLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                ClassLoader.getSystemClassLoader().getParent());

    }

    <T> T apply(final FunctionEx<ClassLoader, T, IOException> classLoaderFunction) throws IOException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return classLoaderFunction.apply(classLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private static URL toUrl(final Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new InternalServerErrorException("Can't convert path to URL: " + path.toAbsolutePath(), e);
        }
    }

    @Override
    public void close() throws IOException {
        classLoader.close();
    }

}
