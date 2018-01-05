PDF parser
==========

This parser extracts the text content from PDF files.


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/pdfbox

```json
{
  "returnedFields" : [ {
    "name" : "title",
    "type" : "STRING",
    "description" : "The title of the Word document"
  }, {
    "name" : "author",
    "type" : "STRING",
    "description" : "The name of the author"
  }, {
    "name" : "subject",
    "type" : "STRING",
    "description" : "The subject of the document"
  }, {
    "name" : "content",
    "type" : "STRING",
    "description" : "The content of the document"
  }, {
    "name" : "producer",
    "type" : "STRING",
    "description" : "The producer of the document"
  }, {
    "name" : "keywords",
    "type" : "STRING",
    "description" : "The keywords of the document"
  }, {
    "name" : "creation_date",
    "type" : "DATE"
  }, {
    "name" : "modification_date",
    "type" : "DATE"
  }, {
    "name" : "language",
    "type" : "STRING"
  }, {
    "name" : "rotation",
    "type" : "INTEGER"
  }, {
    "name" : "number_of_pages",
    "type" : "INTEGER"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "pdf" ],
  "mime_types" : [ "application/pdf" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/pdfbox",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/pdfbox"
  }
}
```