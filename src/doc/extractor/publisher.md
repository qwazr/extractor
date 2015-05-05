Publisher parser
================

This parser extracts the text content and metadata informations from Publisher files (.pub).


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/publisher

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
  "file_extensions" : [ "pub" ],
  "mime_types" : [ "application/x-mspublisher" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/publisher",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/publisher"
  }
}
```