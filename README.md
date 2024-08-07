CLue - Command Line Tool for Apache Lucene
==========================================

## Overview

When working with Lucene, it is often useful to inspect an index.

[Luke](http://www.getopt.org/luke/) is awesome, but often times it is not feasible to inspect an index on a remote
machine using a GUI. That's where Clue comes in.
You can ssh into your production box and inspect your index using your favorite shell.

Another important feature for Clue is the ability to interact with other Unix commands via piping, e.g. grep, more etc.

## About this fork

This is a fork from the upstream version: [javasoze/clue](https://github.com/javasoze/clue/). It tracks the two main releases of Lucene in different
branches:

- `main`: For the latest version (9.X). It requires JDK 11 or newer.
- `branch_8X`: For the 8.X release. It requires JDK 8.

### License

Clue is under the [Apache Public License v2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

### Bugs

Please file bugs and feature requests [here](https://github.com/javasoze/clue/issues).

## Downloads

latest version: 9.11.1-1.0.0

## Build

./mvnw package

This will create the following artifact in the target directory:

```clue-${VERSION}.jar```

## Run

Interactive Mode:

    ./bin/clue.sh my-idx

Non-interactive Mode:

    ./bin/clue.sh my-idx command args

Command list:

    ./bin/clue.sh my-idx help

    using configuration file found at: /Users/johnwang/github/clue/config/clue.conf
	Analyzer: 		class org.apache.lucene.analysis.standard.StandardAnalyzer
	Query Builder: 		class io.dashbase.clue.api.DefaultQueryBuilder
	Directory Builder: 	class io.dashbase.clue.api.DefaultDirectoryBuilder
	IndexReader Factory: 	class io.dashbase.clue.api.DefaultIndexReaderFactory
	count - shows how many documents in index match the given query
	delete - deletes a list of documents from searching via a query, input: query
	directory - prints directory information
	docsetinfo - doc id set info and stats
	docval - gets doc value for a given doc, <field> <docid>, if <docid> not specified, all docs are shown
	exit - exits program
	explain - shows score explanation of a doc
	export - export index to readable text files
	help - displays help
	info - displays information about the index, <segment number> to get information on the segment
	norm - displays norm values for a field for a list of documents
	postings - iterating postings given a term, e.g. <fieldname:fieldvalue>
	readonly - puts clue in readonly mode
	reconstruct - reconstructs an indexed field for a document
	search - executes a query against the index, input: <query string>
	stored - displays stored data for a given field
    showcommitdata - Shows user commit data
	terms - gets terms from the index, <field:term>, term can be a prefix
	tv - shows term vector of a field for a doc

## Build a sample index to play with

Clue bundles with some test data (15000 car data) for you to build a sample index to play with, do:

    ./bin/build_sample_index.sh my-idx

## Examples

1. Getting all the terms in the field 'color_indexed':

   ``./bin/clue.sh my-idx terms color_indexed``

2. Getting all the terms in the field 'color_indexed' starting with the term staring with 'r':

   ``./bin/clue.sh my-idx terms color_indexed:r``

   ``./bin/clue.sh my-idx terms color_indexed | grep r``

3. Do a search:

   ``./bin/clue.sh my-idx search myquery``

4. Get the index info:

   ``./bin/clue.sh my-idx info``

5. Iterate a posting for the term color_indexed:red

   ``./bin/clue.sh my-idx postings color_indexed:red``

6. List docvalues for the column-stride-field color:

   ``./bin/clue.sh my-idx docval color``

7. Get docvalue for the column-stride-field *category* for document 4:

   ``./bin/clue.sh my-idx docval *category* 5``

8. Get docvalue for the column-stride-field *year* of type numeric for document 3:

   ``./bin/clue.sh my-idx docval year 3``

9. Get docvalue for the column-stride-field *json* of type binary for document 3:

   ``./bin/clue.sh my-idx docval json 3``

10. Get docvalue for the column-stride-field *tags* of type sorted-set for document 3:

    ``./bin/clue.sh my-idx docval tags 3``
