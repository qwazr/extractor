RSS parser
==========

This parser extracts the text content and metadata informations from RSS feeds.


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/rss

```json
{
  "returnedFields" : [ {
    "name" : "channel_title",
    "type" : "STRING",
    "description" : "The title of the channel"
  }, {
    "name" : "channel_link",
    "type" : "STRING",
    "description" : "The link of the channel"
  }, {
    "name" : "channel_description",
    "type" : "STRING",
    "description" : "The description of the channel"
  }, {
    "name" : "channel_category",
    "type" : "STRING",
    "description" : "The category of the channel"
  }, {
    "name" : "channel_author_name",
    "type" : "STRING",
    "description" : "The name of the author"
  }, {
    "name" : "channel_author_email",
    "type" : "STRING",
    "description" : "The email address of the author"
  }, {
    "name" : "channel_contributor_name",
    "type" : "STRING",
    "description" : "The name of the contributor"
  }, {
    "name" : "channel_contributor_email",
    "type" : "STRING",
    "description" : "The email address of the contributor"
  }, {
    "name" : "channel_published_date",
    "type" : "STRING",
    "description" : "The published date of the channel"
  }, {
    "name" : "atom_title",
    "type" : "STRING",
    "description" : "The title of the atom"
  }, {
    "name" : "atom_link",
    "type" : "STRING",
    "description" : "The link of the atom"
  }, {
    "name" : "atom_description",
    "type" : "STRING",
    "description" : "The descriptiln of the atom"
  }, {
    "name" : "atom_author_name",
    "type" : "STRING",
    "description" : "The name of the author"
  }, {
    "name" : "atom_author_email",
    "type" : "STRING",
    "description" : "The email address of the author"
  }, {
    "name" : "atom_contributor_name",
    "type" : "STRING",
    "description" : "The name of the contributor"
  }, {
    "name" : "atom_contributor_email",
    "type" : "STRING",
    "description" : "The email address of the contributor"
  }, {
    "name" : "atom_published_date",
    "type" : "STRING",
    "description" : "The published date"
  }, {
    "name" : "atom_updated_date",
    "type" : "STRING",
    "description" : "The updated date"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "rss" ],
  "mime_types" : [ "application/rss+xml" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/rss",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/rss"
  }
}{
  "returnedFields" : [ {
    "name" : "channel_title",
    "type" : "STRING",
    "description" : "The title of the channel"
  }, {
    "name" : "channel_link",
    "type" : "STRING",
    "description" : "The link of the channel"
  }, {
    "name" : "channel_description",
    "type" : "STRING",
    "description" : "The description of the channel"
  }, {
    "name" : "channel_category",
    "type" : "STRING",
    "description" : "The category of the channel"
  }, {
    "name" : "channel_author_name",
    "type" : "STRING",
    "description" : "The name of the author"
  }, {
    "name" : "channel_author_email",
    "type" : "STRING",
    "description" : "The email address of the author"
  }, {
    "name" : "channel_contributor_name",
    "type" : "STRING",
    "description" : "The name of the contributor"
  }, {
    "name" : "channel_contributor_email",
    "type" : "STRING",
    "description" : "The email address of the contributor"
  }, {
    "name" : "channel_published_date",
    "type" : "STRING",
    "description" : "The published date of the channel"
  }, {
    "name" : "atom_title",
    "type" : "STRING",
    "description" : "The title of the atom"
  }, {
    "name" : "atom_link",
    "type" : "STRING",
    "description" : "The link of the atom"
  }, {
    "name" : "atom_description",
    "type" : "STRING",
    "description" : "The descriptiln of the atom"
  }, {
    "name" : "atom_author_name",
    "type" : "STRING",
    "description" : "The name of the author"
  }, {
    "name" : "atom_author_email",
    "type" : "STRING",
    "description" : "The email address of the author"
  }, {
    "name" : "atom_contributor_name",
    "type" : "STRING",
    "description" : "The name of the contributor"
  }, {
    "name" : "atom_contributor_email",
    "type" : "STRING",
    "description" : "The email address of the contributor"
  }, {
    "name" : "atom_published_date",
    "type" : "STRING",
    "description" : "The published date"
  }, {
    "name" : "atom_updated_date",
    "type" : "STRING",
    "description" : "The updated date"
  }, {
    "name" : "lang_detection",
    "type" : "STRING",
    "description" : "Detection of the language"
  } ],
  "file_extensions" : [ "rss" ],
  "mime_types" : [ "application/rss+xml" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/rss",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/rss"
  }
}
```