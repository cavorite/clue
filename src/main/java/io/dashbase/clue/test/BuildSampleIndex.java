package io.dashbase.clue.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.FileSystems;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class BuildSampleIndex {

    static void addMetaString(Document doc, String field, String value) {
        if (value != null) {
            doc.add(new SortedDocValuesField(field, new BytesRef(value)));
            doc.add(new StringField(field + "_indexed", value, Store.YES));
        }
    }

    static Document buildDoc(JsonNode json) throws Exception {
        Document doc = new Document();
        doc.add(new NumericDocValuesField("id", json.get("id").longValue()));
        doc.add(
                new StringField(
                        "id_indexed",
                        String.format("id-%s", json.get("id").longValue()),
                        Store.NO));
        doc.add(new DoubleDocValuesField("price", json.get("price").doubleValue()));
        doc.add(new TextField("contents", json.get("contents").textValue(), Store.NO));
        doc.add(new NumericDocValuesField("year", json.get("year").longValue()));
        doc.add(new NumericDocValuesField("mileage", json.get("mileage").longValue()));

        addMetaString(doc, "color", json.get("color").textValue());
        addMetaString(doc, "category", json.get("category").textValue());
        addMetaString(doc, "makemodel", json.get("makemodel").textValue());
        addMetaString(doc, "city", json.get("city").textValue());

        String tagsString = json.get("tags").textValue();
        if (tagsString != null) {
            String[] parts = tagsString.split(",");
            for (String part : parts) {
                doc.add(new SortedSetDocValuesField("tags", new BytesRef(part)));
                doc.add(new StringField("tags_indexed", part, Store.NO));
            }

            // store everything
            FieldType ft = new FieldType();
            ft.setOmitNorms(false);
            ft.setTokenized(true);
            ft.setStoreTermVectors(true);
            ft.setStoreTermVectorOffsets(true);
            ft.setStoreTermVectorPayloads(true);
            ft.setStoreTermVectorPositions(true);
            ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

            Field tagPayload = new Field("tags_payload", new PayloadTokenizer(tagsString), ft);
            doc.add(tagPayload);
        }

        doc.add(new BinaryDocValuesField("json", new BytesRef(json.toString())));

        return doc;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("usage: source_file index_dir");
            System.exit(1);
        }
        File f = new File(args[0]);
        BufferedReader reader = new BufferedReader(new FileReader(f));

        ObjectMapper mapper = new ObjectMapper();

        IndexWriterConfig idxWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
        Directory dir = FSDirectory.open(FileSystems.getDefault().getPath(args[1]));
        IndexWriter writer = new IndexWriter(dir, idxWriterConfig);
        int count = 0;
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            JsonNode json = mapper.readTree(line);
            Document doc = buildDoc(json);
            writer.addDocument(doc);
            count++;
            if (count % 100 == 0) {
                System.out.print(".");
            }
        }

        System.out.println(count + " docs indexed");

        reader.close();
        writer.commit();
        writer.close();
    }
}
