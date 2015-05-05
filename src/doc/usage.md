Usage
=====

Obtain the parser list
----------------------

* Method: GET
* URL: http://{hostname}:{port}/

```shell
curl -XGET http://localhost:9091
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
* URL: http://{hostname}:{port}/{parser_name}

```shell
curl -XGET http://localhost:9091/text
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
* URL: http://{hostname}:{port}/{parser_name}
* Payload: The document

```shell
curl -XPUT --data-binary @tutorial.pdf http://localhost:9091/pdfbox
```
 
#### Using parser auto-detection

* Method: PUT
* URL: http://{hostname}:{port}?name={file_name}
* Payload: The document

```shell
curl -XPUT --data-binary @tutorial.pdf http://localhost:9091?name=tutorial.pdf
```
 
### Extract from document already present on the server

If the file is already available in the server, the extraction can made by passing the path of the file.

#### Using a specified parser

* Method: GET
* URL: http://{hostname}:{port}/{parser_name}?path=file_path

```shell
curl -XGET http://localhost:9091/pdfbox?path=/home/manu/tutorial.pdf
```

#### Using parser auto-detection

* Method: PUT
* URL: http://{hostname}:{port}?path={file_path}

```shell
curl -XGET http://localhost:9091?path=/home/manu/tutorial.pdf
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