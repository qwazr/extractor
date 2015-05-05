PPTX parser
==========

This parser extracts the text content and metadata informations from Powerpoint files.


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/pptx

```json
{
  "returnedFields" : [ {
    "name" : "title",
    "type" : "STRING",
    "description" : "The title of the document"
  }, {
    "name" : "creator",
    "type" : "STRING",
    "description" : "The name of the creator"
  }, {
    "name" : "description",
    "type" : "STRING"
  }, {
    "name" : "keywords",
    "type" : "STRING"
  }, {
    "name" : "subject",
    "type" : "STRING",
    "description" : "The subject of the document"
  }, {
    "name" : "creation_date",
    "type" : "DATE"
  }, {
    "name" : "modification_date",
    "type" : "DATE"
  }, {
    "name" : "slides",
    "type" : "STRING"
  }, {
    "name" : "master",
    "type" : "STRING"
  }, {
    "name" : "notes",
    "type" : "STRING"
  }, {
    "name" : "comments",
    "type" : "STRING"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "pptx" ],
  "mime_types" : [ "application/vnd.openxmlformats-officedocument.presentationml.presentation" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/pptx",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/pptx"
  }
}
```