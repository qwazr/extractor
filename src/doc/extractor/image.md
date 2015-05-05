IMAGE parser
==========

This parser extract various metadata informations from Images files.
It generates also a perceptual hash.

Supported formats
-----------------

- Bmp
- Gif
- Jpeg (.jpg, .jpeg)
- Png
- Wbmp


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/image

```json
{
  "returnedFields" : [ {
    "name" : "width",
    "type" : "INTEGER",
    "description" : "Width of the image in pixels"
  }, {
    "name" : "height",
    "type" : "INTEGER",
    "description" : "Height of the image in pixels"
  }, {
    "name" : "format",
    "type" : "STRING",
    "description" : "The detected format"
  }, {
    "name" : "phash",
    "type" : "STRING",
    "description" : "Perceptual Hash"
  } ],
  "file_extensions" : [ "bmp", "jpg", "wbmp", "jpeg", "png", "gif" ],
  "mime_types" : [ "image/jpeg", "image/png", "image/x-png", "image/vnd.wap.wbmp", "image/bmp", "image/gif" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/image",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/image"
  }
}
```