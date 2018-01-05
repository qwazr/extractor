Visio parser
============

This parser extracts the text content and metadata informations from Visio files (.vsd).


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/visio

```json
{
  "returnedFields" : [ {
    "name" : "title",
    "type" : "STRING",
    "description" : "The title of the document"
  }, {
    "name" : "author",
    "type" : "STRING",
    "description" : "The name of the author"
  }, {
    "name" : "creation_date",
    "type" : "DATE"
  }, {
    "name" : "modification_date",
    "type" : "DATE"
  }, {
    "name" : "keywords",
    "type" : "STRING"
  }, {
    "name" : "subject",
    "type" : "STRING",
    "description" : "The subject of the document"
  }, {
    "name" : "content",
    "type" : "STRING",
    "description" : "The content of the document"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "vsd" ],
  "mime_types" : [ "application/vnd.visio" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/visio",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/visio"
  }
}
```