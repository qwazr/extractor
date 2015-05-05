DOC parser
==========

This parser extracts the text content and metadata informations from Word files (.doc, .dot).


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/doc

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
    "name" : "subject",
    "type" : "STRING",
    "description" : "The subject of the document"
  }, {
    "name" : "keywords",
    "type" : "STRING"
  }, {
    "name" : "content",
    "type" : "STRING",
    "description" : "The content of the document"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "doc", "dot" ],
  "mime_types" : [ "application/msword" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/doc",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/doc"
  }
}
```