RTF parser
==========

This parser extracts the text content and metadata informations from Rich Text Format files.


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/rtf

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
  } ],
  "file_extensions" : [ "rtf", "rtx" ],
  "mime_types" : [ "application/rtf", "text/richtext" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/rtf",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/rtf"
  }
}
```