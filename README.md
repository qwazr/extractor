QWAZR Extractor
===============

Text and meta-data extraction services services for [QWAZR](https://www.qwazr.com)

The extractor module exposes REST/JSON API for text and meta-data extraction.
Here is the list of supported binary formats. Click on the link to see the description of the returned informations.

- Word processor: [doc](src/doc/extractor/doc.md), [docx](src/doc/extractor/docx.md), [odt](src/doc/extractor/odt.md), [rtf](src/doc/extractor/rtf.md)
- Spreadsheet: [xls](src/doc/extractor/xls.md), [xlsx](src/doc/extractor/xlsx.md), [ods](src/doc/extractor/odf.md)
- Presentation: [ppt](src/doc/extractor/ppt.md), [pptx](src/doc/extractor/pptx.md), [odp](src/doc/extractor/odf.md)
- Publishing: [pdf](src/doc/extractor/pdfbox.md), [pub](src/doc/extractor/publisher.md)
- Web: [rss](src/doc/extractor/rss.md), [html/xhtml](src/doc/extractor/html.md)
- Medias: [audio](src/doc/extractor/audio.md), [images](src/doc/extractor/image.md)
- Others: [vsd](src/doc/extractor/visio.md), [text](src/doc/extractor/text.md), [markdown](src/doc/extractor/markdown.md)

Usage
=====

Obtain the parser list
----------------------

* Method: GET
* URL: http://{hostname}:{port}/extractor

```shell
curl -XGET http://localhost:9091/extractor
```

The function return the list of available parsers.

```json
{
  "audio" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "audio"
    }
  },
  "doc" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "doc"
    }
  },
  "docx" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "docx"
    }
  },
  "eml" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "eml"
    }
  },
  "image" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "image"
    }
  },
  "odf" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "odf"
    }
  },
  "mapimsg" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "mapimsg"
    }
  },
  "markdown" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "markdown"
    }
  },
  "pdfbox" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "pdfbox"
    }
  },
  "ppt" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "ppt"
    }
  },
  "pptx" : {
    "_link" : {
      "method" : "GET",  "rel" : "describe",  "href" : "pptx"
    }
  },
  "publisher" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "publisher"
    }
  },
  "rss" : {
    "_link" : {
      "method" : "GET", "rel" : "describe",  "href" : "rss"
    }
  },
  "rtf" : {
    "_link" : {
      "method" : "GET", "rel" : "describe", "href" : "rtf"
    }
  },
  "text" : {
    "_link" : {
      "method" : "GET",   "rel" : "describe", "href" : "text"
    }
  },
  "visio" : {
    "_link" : {
      "method" : "GET", "rel" : "describe",  "href" : "visio"
    }
  },
  "xls" : {
    "_link" : {
      "method" : "GET",  "rel" : "describe",  "href" : "xls"
    }
  },
  "xlsx" : {
    "_link" : {
      "method" : "GET",  "rel" : "describe", "href" : "xlsx"
    }
  }
}
```

Get information about a parser
------------------------------

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
  "mime_types" : [ "text/plain" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/text",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/text"
  }
}
```

Submit a document
-----------------

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

### The returned informations

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

Have a look at the [Rtf](https://github.com/qwazr/qwazr-extractor/blob/master/src/main/java/com/qwazr/extractor/parser/rtf.java) class to see a simple example.

```java
	@Override
	protected void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception {

		// Extract the text data
		RTFEditorKit rtf = new RTFEditorKit();
		Document doc = rtf.createDefaultDocument();
		rtf.read(inputStream, doc, 0);

		// Fill the metas
		metas.add(TITLE, "title of the document");

		// Obtain a new parser document.
		ParserDocument result = getNewParserDocument();

		// Fill the field of the ParserDocument
		result.add(CONTENT, doc.getText(0, doc.getLength()));

		// Apply the language detection
		result.add(LANG_DETECTION, languageDetection(CONTENT, 10000));

	}
```