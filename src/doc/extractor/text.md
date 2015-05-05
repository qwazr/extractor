TEXT parser
===========

This parser extracts the text content and try to detect the language and the charset.


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/text

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