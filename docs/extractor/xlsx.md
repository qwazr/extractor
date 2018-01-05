XLSX parser
===========

This parser extracts the text content and metadata informations from Excel files.


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/xlsx

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
    "name" : "creation_date",
    "type" : "DATE"
  }, {
    "name" : "modification_date",
    "type" : "DATE"
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
    "name" : "content",
    "type" : "STRING",
    "description" : "The content of the document"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "xlsx" ],
  "mime_types" : [ "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/xlsx",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/xlsx"
  }
}
```