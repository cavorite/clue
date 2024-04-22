// Pass the JavaImporter object to the "with" statement and access the classes
// from the imported packages by their simple names within the statement's body

var stdAnalyzer = new StandardAnalyzer();

var custom = CustomAnalyzer.builder()
    .addTokenFilter("lowercase")
    .withTokenizer("standard")
    .build()

var forQuery = new PerFieldAnalyzerWrapper(stdAnalyzer, {
        "firstName": new KeywordAnalyzer(),
        "zzz" : custom
    });

var forIndexing = new StandardAnalyzer();