package io.dashbase.clue.commands;

import io.dashbase.clue.LuceneContext;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

@Readonly
public class DocSetInfoCommand extends ClueCommand {

    private static final int DEFAULT_BUCKET_SIZE = 1000;

    private final LuceneContext ctx;

    public DocSetInfoCommand(LuceneContext ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return "docsetinfo";
    }

    @Override
    public String help() {
        return "doc id set info and stats";
    }

    private static double[] PERCENTILES = new double[] {50.0, 75.0, 90.0, 95.0, 99.0};

    @Override
    protected ArgumentParser buildParser(ArgumentParser parser) {
        parser.addArgument("-f", "--field").required(true).help("field/term pair, e.g. field:term");
        parser.addArgument("-s", "--size")
                .type(Integer.class)
                .setDefault(DEFAULT_BUCKET_SIZE)
                .help("bucket size");
        return parser;
    }

    @Override
    public void execute(Namespace args, PrintStream out) throws Exception {
        String field = args.getString("field");
        String termVal = null;
        int bucketSize = args.getInt("size");

        if (field != null) {
            String[] parts = field.split(":");
            if (parts.length > 1) {
                field = parts[0];
                termVal = parts[1];
            }
        }

        IndexReader reader = ctx.getIndexReader();
        List<LeafReaderContext> leaves = reader.leaves();

        PostingsEnum postingsEnum = null;
        for (LeafReaderContext leaf : leaves) {
            LeafReader atomicReader = leaf.reader();
            Terms terms = atomicReader.terms(field);
            if (terms == null) {
                continue;
            }
            if (terms != null && termVal != null) {
                TermsEnum te = terms.iterator();

                if (te.seekExact(new BytesRef(termVal))) {
                    postingsEnum = te.postings(postingsEnum, PostingsEnum.FREQS);

                    int docFreq = te.docFreq();

                    int minDocId = -1, maxDocId = -1;
                    int doc, count = 0;

                    int[] percentDocs = new int[PERCENTILES.length];

                    int percentileIdx = 0;

                    while ((doc = postingsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        maxDocId = doc;
                        if (minDocId == -1) {
                            minDocId = doc;
                        }
                        count++;

                        double perDocs = (double) count / (double) docFreq * 100.0;
                        while (percentileIdx < percentDocs.length) {
                            if (perDocs > PERCENTILES[percentileIdx]) {
                                percentDocs[percentileIdx] = doc;
                                percentileIdx++;
                            } else {
                                break;
                            }
                        }
                    }

                    // calculate histogram
                    int[] buckets = null;
                    if (maxDocId > 0) {
                        buckets = new int[maxDocId / bucketSize + 1];

                        postingsEnum = te.postings(postingsEnum, PostingsEnum.FREQS);
                        while ((doc = postingsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                            int bucketIdx = doc / bucketSize;
                            buckets[bucketIdx]++;
                        }
                    }

                    double density = (double) docFreq / (double) (maxDocId - minDocId);
                    out.println(
                            String.format(
                                    "min: %d, max: %d, count: %d, density: %.2f",
                                    minDocId, maxDocId, docFreq, density));
                    out.println(
                            "percentiles: "
                                    + Arrays.toString(PERCENTILES)
                                    + " => "
                                    + Arrays.toString(percentDocs));
                    out.println("histogram: (bucketsize=" + bucketSize + ")");
                    out.println(Arrays.toString(buckets));
                }
            }
        }
    }
}
