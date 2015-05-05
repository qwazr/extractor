Contribute
==========

Writing a parser is easy. Just extends the abstract class [ParserAbstract](https://github.com/qwazr/qwazr-extractor/blob/master/src/main/java/com/qwazr/extractor/ParserAbstract.java) and implements the required methods.

```java
protected void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception;
```

The parse must build a list of ParserDocument. A parser may return one or more documents (one document per page, one document per RSS item, ...). A Parser Document is a list of name/value pair.

Have a look at the [Rtf](https://github.com/qwazr/qwazr-extractor/blob/master/src/main/java/com/qwazr/extractor/parser/rtf.java) class to see a simple example.

```java
	@Override
	protected void parseContent(InputStream inputStream, String extension, String mimeType) throws Exception {

		// Extract the text data
		RTFEditorKit rtf = new RTFEditorKit();
		Document doc = rtf.createDefaultDocument();
		rtf.read(inputStream, doc, 0);

		// Fill the metas
		metas.add(TITLE, "title of the document");
		
		// Obtain a new parser document.
		ParserDocument result = getNewParserDocument();

		// Fill the field of the ParserDocument
		result.add(CONTENT, doc.getText(0, doc.getLength()));

		// Apply the language detection
		result.add(LANG_DETECTION, languageDetection(CONTENT, 10000));

	}
```