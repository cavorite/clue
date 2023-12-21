package io.dashbase.clue;

import io.dashbase.clue.api.*;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public class LuceneContext extends ClueContext {
    private final IndexReaderFactory readerFactory;

    private IndexWriter writer;
    private final Directory directory;
    private final IndexWriterConfig writerConfig;
    private final QueryBuilder queryBuilder;
    private final AnalyzerFactory analyzerFactory;
    private final BytesRefDisplay termBytesRefDisplay;
    private final BytesRefDisplay payloadBytesRefDisplay;

    public LuceneContext(String dir, ClueAppConfiguration config, boolean interactiveMode)
            throws Exception {
        super(config.commandRegistrar, interactiveMode);
        this.directory = config.dirBuilder.build(dir);
        this.analyzerFactory = config.analyzerFactory;
        this.readerFactory = config.indexReaderFactory;
        this.readerFactory.initialize(directory);
        this.queryBuilder = config.queryBuilder;
        this.queryBuilder.initialize("contents", analyzerFactory.forQuery());
        this.writerConfig = new IndexWriterConfig(new StandardAnalyzer());
        this.termBytesRefDisplay = new StringBytesRefDisplay();
        this.payloadBytesRefDisplay = new StringBytesRefDisplay();
        this.writer = null;
    }

    public IndexReader getIndexReader() {
        return readerFactory.getIndexReader();
    }

    public IndexSearcher getIndexSearcher() {
        return new IndexSearcher(getIndexReader());
    }

    public IndexWriter getIndexWriter() {
        if (registry.isReadonly()) return null;
        if (writer == null) {
            try {
                writer = new IndexWriter(directory, writerConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return writer;
    }

    Collection<String> fieldNames() {
        LinkedList<String> fieldNames = new LinkedList<>();
        for (LeafReaderContext context : getIndexReader().leaves()) {
            LeafReader reader = context.reader();
            for (FieldInfo info : reader.getFieldInfos()) {
                fieldNames.add(info.name);
            }
        }
        return fieldNames;
    }

    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public Analyzer getAnalyzerQuery() {
        return analyzerFactory.forQuery();
    }

    public BytesRefDisplay getTermBytesRefDisplay() {
        return termBytesRefDisplay;
    }

    public BytesRefDisplay getPayloadBytesRefDisplay() {
        return payloadBytesRefDisplay;
    }

    public Directory getDirectory() {
        return directory;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.registry.setReadonly(readOnlyMode);
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = null;
        }
    }

    public void refreshReader() throws Exception {
        readerFactory.refreshReader();
    }

    @Override
    public void shutdown() throws Exception {
        try {
            readerFactory.shutdown();
        } finally {
            directory.close();
            if (writer != null) {
                writer.close();
            }
        }
    }
}
