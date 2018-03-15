WDP parser
==========

This parser extracts the text content and metadata informations from WordPerfect files (.wpd, .wp6, .wp5, ...).


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/wdp

```json
{
  "returnedFields" : [ {
    "name" : "title",
    "type" : "STRING",
    "description" : "The optional title of the document"
  }, {
    "name" : "content",
    "type" : "STRING",
    "description" : "The content of the document"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "fileExtensions" : [ "wpd", "w60", "w61", "wp", "wp5", "wp6" ],
  "mimeTypes" : [ "application/wordperfect", "application/wordperfect6.0", "application/wordperfect6.1" ]
}
```