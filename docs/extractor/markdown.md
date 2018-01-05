Markdown parser
===============

This parser extracts the text content from Markdown files (.md, .markdown).


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/markdown

```json
{
  "returnedFields" : [ {
    "name" : "content",
    "type" : "STRING",
    "description" : "The content of the document"
  }, {
    "name" : "url",
    "type" : "STRING",
    "description" : "Detected URLs"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "md", "markdown" ],
  "mime_types" : [ "text/x-markdown", "text/markdown" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/markdown",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/markdown"
  }
}
```