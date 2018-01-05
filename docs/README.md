QWAZR Extractor
===============

[![Build Status](https://travis-ci.org/qwazr/extractor.svg?branch=master)](https://travis-ci.org/qwazr/extractor)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.qwazr/qwazr-profiler/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.qwazr/qwazr-extractor)
[![Coverage Status](https://coveralls.io/repos/github/qwazr/extractor/badge.svg?branch=master)](https://coveralls.io/github/qwazr/extractor?branch=master)
[![Join the chat at https://gitter.im/qwazr/QWAZR](https://badges.gitter.im/qwazr/QWAZR.svg)](https://gitter.im/qwazr/QWAZR)

Text and meta-data extraction service.
The extractor module exposes a JSON web service for text and meta-data extraction.
Here is the list of supported binary formats. Click on the link to see the description of the returned informations.

- Word processor: [doc](extractor/doc.md), [docx](extractor/docx.md), [odt](extractor/odt.md), [rtf](extractor/rtf.md)
- Spreadsheet: [xls](extractor/xls.md), [xlsx](extractor/xlsx.md), [ods](extractor/odf.md)
- Presentation: [ppt](extractor/ppt.md), [pptx](extractor/pptx.md), [odp](extractor/odf.md)
- Publishing: [pdf](extractor/pdfbox.md), [pub](extractor/publisher.md)
- Web: [rss](extractor/rss.md), [html/xhtml](extractor/html.md)
- Medias: [audio](extractor/audio.md), [images](extractor/image.md)
- Others: [vsd](extractor/visio.md), [text](extractor/text.md), [markdown](extractor/markdown.md)

Usage
=====

## Run using Docker

    docker run -p 9091:9091 qwazr/extractor
    
    
### Obtain the parser list

* Method: GET
* URL: http://{hostname}:{port}/extractor

```shell
curl -XGET http://localhost:9091/extractor
```

The function returns the list of available parsers.

```json
[
"audio",
"doc",
"docx",
"eml",
"html",
"image",
"mapimsg",
"markdown",
"ocr",
"odf",
"pdfbox",
"ppt",
"pptx",
"publisher",
"rss",
"rtf",
"text",
"visio",
"xls",
"xlsx"
]
```

### Get information about a parser

* Method: GET
* URL: http://{hostname}:{port}/extractor/{parser_name}

```shell
curl -XGET http://localhost:9091/extractor/text
```

The function displays which fields are returned by the parser and the available methods.

```json
{
  "returnedFields" : [ {
    "name" : "content",
    "type" : "STRING",
    "description" : "The content of the document"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  }, {
    "name" : "charset_detection",
    "type" : "STRING",
    "description" : "Detection of the charset"
  } ],
  "file_extensions" : [ "txt" ],
  "mime_types" : [ "text/plain" ]
}
```

### Submit a document

There are several options to extract data from a file.

1. The location of the file:
	- The file may be uploaded (PUT)
	- The file is already available on the the server (GET)
2. The parser selection:
	- Specify a parser
	- Use the auto detection system.


### Extract data by uploading a document

#### Using a specified parser

* Method: PUT
* URL: http://{hostname}:{port}/extractor/{parser_name}
* Payload: The document

```shell
curl -XPUT --data-binary @tutorial.pdf http://localhost:9091/extractor/pdfbox
```

#### Using parser auto-detection

* Method: PUT
* URL: http://{hostname}:{port}/extractor?name={file_name}
* Payload: The document

```shell
curl -XPUT --data-binary @tutorial.pdf http://localhost:9091/extractor?name=tutorial.pdf
```

### Extract from document already present on the server

If the file is already available in the server, the extraction can made by passing the path of the file.

#### Using a specified parser

* Method: GET
* URL: http://{hostname}:{port}/extractor/{parser_name}?path=file_path

```shell
curl -XGET http://localhost:9091/extractor/pdfbox?path=/home/manu/tutorial.pdf
```

#### Using parser auto-detection

* Method: PUT
* URL: http://{hostname}:{port}/extractor?path={file_path}

```shell
curl -XGET http://localhost:9091/extractor?path=/home/manu/tutorial.pdf
```

#### The returned informations

The parser extracts the metas and text information using the following JSON format:

```json
{
	"time_elapsed": 2735,
	"metas": {
		"number_of_pages": [7],
		"producer": ["FOP 0.20.5"]
	},
	"documents": [
		{
			"content": ["Table of contents Requirements Getting Started Deleting Querying Data Sorting Text  Analysis Debugging"],
			"character_count":[13634],
			"rotation": [ 0 ],
			"lang_detection": ["en" ]
		}
	]
}
```


Contribute
==========

Writing a parser is easy. Just extends the abstract class [ParserAbstract](src/main/java/com/qwazr/extractor/ParserAbstract.java) and implements the required methods.

```java
protected void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception;
```

The parse must build a list of ParserDocument. A parser may return one or more documents (one document per page, one document per RSS item, ...). A Parser Document is a list of name/value pair.

Have a look at the [Rtf](https://github.com/qwazr/extractor/blob/master/src/main/java/com/qwazr/extractor/parser/Rtf.java) class to see a simple example.

```java

	@Override
	protected void parseContent(InputStream inputStream, String extension,
			String mimeType) throws Exception {

		// Extract the text data
		RTFEditorKit rtf = new RTFEditorKit();
		Document doc = rtf.createDefaultDocument();
		rtf.read(inputStream, doc, 0);

		// Obtain a new parser document.
		ParserDocument result = getNewParserDocument();

		// Fill the field of the ParserDocument
		result.add(CONTENT, doc.getText(0, doc.getLength()));

		// Apply the language detection
		result.add(LANG_DETECTION, languageDetection(CONTENT, 10000));

	}
```