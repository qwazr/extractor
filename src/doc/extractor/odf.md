Open Office Document
====================

This parser extracts the text content and metadata informations from OpenOffice files.

Supported format
----------------

- OpenOffice Calc (.ods, .ots)
- OpenOffice Writer (.odt, .ott)
- OpenOffice Impress (.odp, .otp)

Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/odf

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
    "type" : "DATE",
    "description" : "The date of creation"
  }, {
    "name" : "modification_date",
    "type" : "DATE",
    "description" : "The date of last modification"
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
    "name" : "language",
    "type" : "STRING"
  }, {
    "name" : "producer",
    "type" : "STRING",
    "description" : "The producer of the document"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "ods", "ots", "odt", "odm", "ott", "odp", "otp" ],
  "mime_types" : [ "application/vnd.oasis.opendocument.spreadsheet", "application/vnd.oasis.opendocument.spreadsheet-template", "application/vnd.oasis.opendocument.text", "application/vnd.oasis.opendocument.text-master", "application/vnd.oasis.opendocument.text-template", "application/vnd.oasis.opendocument.presentation", "application/vnd.oasis.opendocument.presentation-template" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/odf",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/odf"
  }
}
```