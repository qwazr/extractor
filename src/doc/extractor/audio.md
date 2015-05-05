The AUDIO parser
================

This parser extract various metadata informations from audio files.

Format supported
----------------

- Vorbis: .ogg
- Mpeg (mp3, mp4, m4a, m4p)
- Flag
- Windows (wma, wav)
- Real Audio (ra, rm)


Get the parser properties
-------------------------

     curl -XGET http://localhost:9091/audio

```json
{
  "returnedFields" : [ {
    "name" : "album",
    "type" : "STRING"
  }, {
    "name" : "album_artist",
    "type" : "STRING"
  }, {
    "name" : "album_artist_sort",
    "type" : "STRING"
  }, {
    "name" : "album_sort",
    "type" : "STRING"
  }, {
    "name" : "amazon_id",
    "type" : "STRING"
  }, {
    "name" : "arranger",
    "type" : "STRING"
  }, {
    "name" : "artist",
    "type" : "STRING"
  }, {
    "name" : "artist_sort",
    "type" : "STRING"
  }, {
    "name" : "barcode",
    "type" : "STRING"
  }, {
    "name" : "bpm",
    "type" : "STRING"
  }, {
    "name" : "catalog_no",
    "type" : "STRING"
  }, {
    "name" : "comment",
    "type" : "STRING"
  }, {
    "name" : "composer",
    "type" : "STRING"
  }, {
    "name" : "composer_sort",
    "type" : "STRING"
  }, {
    "name" : "conductor",
    "type" : "STRING"
  }, {
    "name" : "cover_art",
    "type" : "STRING"
  }, {
    "name" : "custom1",
    "type" : "STRING"
  }, {
    "name" : "custom2",
    "type" : "STRING"
  }, {
    "name" : "custom3",
    "type" : "STRING"
  }, {
    "name" : "custom4",
    "type" : "STRING"
  }, {
    "name" : "custom5",
    "type" : "STRING"
  }, {
    "name" : "disc_no",
    "type" : "STRING"
  }, {
    "name" : "disc_total",
    "type" : "STRING"
  }, {
    "name" : "djmixer",
    "type" : "STRING"
  }, {
    "name" : "encoder",
    "type" : "STRING"
  }, {
    "name" : "engineer",
    "type" : "STRING"
  }, {
    "name" : "fbpm",
    "type" : "STRING"
  }, {
    "name" : "genre",
    "type" : "STRING"
  }, {
    "name" : "grouping",
    "type" : "STRING"
  }, {
    "name" : "is_compilation",
    "type" : "STRING"
  }, {
    "name" : "isrc",
    "type" : "STRING"
  }, {
    "name" : "key",
    "type" : "STRING"
  }, {
    "name" : "language",
    "type" : "STRING"
  }, {
    "name" : "lyricist",
    "type" : "STRING"
  }, {
    "name" : "lyrics",
    "type" : "STRING"
  }, {
    "name" : "media",
    "type" : "STRING"
  }, {
    "name" : "mixer",
    "type" : "STRING"
  }, {
    "name" : "mood",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_artistid",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_disc_id",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_release_country",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_release_group_id",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_release_status",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_release_type",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_releaseartistid",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_releaseid",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_track_id",
    "type" : "STRING"
  }, {
    "name" : "musicbrainz_work_id",
    "type" : "STRING"
  }, {
    "name" : "musicip_id",
    "type" : "STRING"
  }, {
    "name" : "occasion",
    "type" : "STRING"
  }, {
    "name" : "original_album",
    "type" : "STRING"
  }, {
    "name" : "original_artist",
    "type" : "STRING"
  }, {
    "name" : "original_lyricist",
    "type" : "STRING"
  }, {
    "name" : "original_year",
    "type" : "STRING"
  }, {
    "name" : "producer",
    "type" : "STRING"
  }, {
    "name" : "quality",
    "type" : "STRING"
  }, {
    "name" : "rating",
    "type" : "STRING"
  }, {
    "name" : "record_label",
    "type" : "STRING"
  }, {
    "name" : "remixer",
    "type" : "STRING"
  }, {
    "name" : "script",
    "type" : "STRING"
  }, {
    "name" : "tags",
    "type" : "STRING"
  }, {
    "name" : "tempo",
    "type" : "STRING"
  }, {
    "name" : "title",
    "type" : "STRING"
  }, {
    "name" : "title_sort",
    "type" : "STRING"
  }, {
    "name" : "track",
    "type" : "STRING"
  }, {
    "name" : "track_total",
    "type" : "STRING"
  }, {
    "name" : "url_discogs_artist_site",
    "type" : "STRING"
  }, {
    "name" : "url_discogs_release_site",
    "type" : "STRING"
  }, {
    "name" : "url_lyrics_site",
    "type" : "STRING"
  }, {
    "name" : "url_official_artist_site",
    "type" : "STRING"
  }, {
    "name" : "url_official_release_site",
    "type" : "STRING"
  }, {
    "name" : "url_wikipedia_artist_site",
    "type" : "STRING"
  }, {
    "name" : "url_wikipedia_release_site",
    "type" : "STRING"
  }, {
    "name" : "year",
    "type" : "STRING"
  } ],
  "file_extensions" : [ "ogg", "mp3", "flac", "mp4", "m4a", "m4p", "wma", "wav", "ra", "rm", "m4b" ],
  "mime_types" : [ "audio/x-wav", "audio/wav", "audio/x-pn-realaudio", "audio/mp4", "audio/vnd.rn-realaudio", "audio/ogg", "audio/x-realaudio", "audio/mpeg3", "audio/flac", "audio/mpeg" ],
  "_link1" : {
    "method" : "GET",
    "rel" : "parse local file",
    "href" : "/audio",
    "queryString" : [ {
      "name" : "path",
      "type" : "STRING",
      "description" : "path to the local file"
    }, {
      "name" : "format",
      "type" : "STRING",
      "description" : "Supported format: ogg, mp3, flac, mp4, m4a, m4p, wma, wav, ra, rm, m4b"
    } ]
  },
  "_link2" : {
    "method" : "PUT",
    "rel" : "upload",
    "href" : "/audio",
    "queryString" : [ {
      "name" : "format",
      "type" : "STRING",
      "description" : "Supported format: ogg, mp3, flac, mp4, m4a, m4p, wma, wav, ra, rm, m4b"
    } ]
  }
}
```