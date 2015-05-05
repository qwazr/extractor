QWAZR Extractor
===============

An open source RESTFul Web Service for text extraction and analysis. A component of QWAZR.
**oss-extractor** supports various binary formats.

- Word processor (doc, docx, odt, rtf)
- Spreadsheet (xls, xlsx, ods)
- Presentation (ppt, pptx, odp)
- Publishing (pdf, pub)
- Web (rss, html/xhtml)
- Medias (audio, images)
- Others (vsd, text, markdown)

Links
-----

- [Home page](http://www.opensearchserver.com/oss-extractor/README.md)
- [Installation](http://www.opensearchserver.com/oss-extractor/installation.md)
- [Usage](http://www.opensearchserver.com/oss-extractor/usage.md)
- [Extractor list in alphabetical order](http://www.opensearchserver.com/oss-extractor/extractor/README.md)
- [Source code](https://github.com/opensearchserver/oss-extractor)
- [Compile and build](http://www.opensearchserver.com/oss-extractor/compile-and-build.md)
- [How to contribute](http://www.opensearchserver.com/oss-extractor/contribute.md)

Quickstart
----------

### Requires JAVA

Check that you have installed a [JAVA Runtime Environment 7 or newer](http://openjdk.java.net/install/)

### Download or compile the JAR:

#### Download:

The [binary archives](http://sourceforge.net/projects/oss-extractor/files/v1.1/) are available at SourceForge

To follow this quickstart please download [oss-extractor-1.1-exec.jar](http://sourceforge.net/projects/oss-extractor/files/v1.1/oss-extractor-1.1.0-exec.jar/download)

#### Or clone and compile:

The compilation and packaging requires [Maven 3.0 or newer](http://maven.apache.org/)

Clone the source code:

```shell
git clone https://github.com/opensearchserver/oss-extractor.git
```

Compile and package (the binary will located in the target directory):

```shell
mvn clean package
```

### Usage

#### Start the server

```shell
java -jar target/oss-extractor-xxx-exec.jar
```

#### Obtain the parser list

```shell
curl -XGET http://localhost:9091
```

#### Get information about a parser

```shell
curl -XGET http://localhost:9091/pdfbox
```
    
#### Submit a document to a parser

By uploading a document:

```shell
curl -XPUT --data-binary @tutorial.pdf http://localhost:9091/pdfbox
```
    
If the file is already available in the server, the follow API can be used:

```shell
curl -XGET http://localhost:9091/pdfbox?path=/home/user/myfile.pdf
```

Issues and change Log
---------------------

Issues and milestones are tracked on GitHub:

- [Open issues](https://github.com/qwazr/qwazr-extractor/issues?q=is%3Aopen+is%3Aissue)
- [Closed issues](https://github.com/qwazr/qwazr-extractor/issues?q=is%3Aissue+is%3Aclosed)

License
-------

Copyright 2014-2015 [OpenSearchServer Inc.](http://www.opensearchserver.com)


Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.