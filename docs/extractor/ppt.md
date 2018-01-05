PPT parser
==========

This parser extracts the text content and metadata informations from Powerpoint files (.ppt).


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/ppt

```json
{
  "returnedFields" : [ {
    "name" : "title",
    "type" : "STRING",
    "description" : "The title of the document"
  }, {
    "name" : "body",
    "type" : "STRING",
    "description" : "The body of the document"
  }, {
    "name" : "notes",
    "type" : "STRING"
  }, {
    "name" : "other",
    "type" : "STRING"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "ppt" ],
  "mime_types" : [ "application/vnd.ms-powerpoint" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/ppt",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/ppt"
  }
}
```