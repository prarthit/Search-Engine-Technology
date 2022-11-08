package cecs429.querying;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.indexing.diskIndex.DocWeightsWriter;
import cecs429.text.TokenProcessor;

class Pair implements Comparable<Pair> {
    Posting first;
    double second;

    Pair(Posting first, double second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(Pair p) {
        // Multiply with -1 to compare in descending order
        return -1 * Double.compare(this.second, p.second);
    }
}

public class RankedQuerySearch extends QueryResults {
    private int k = 10;

    public RankedQuerySearch() {
    };

    public RankedQuerySearch(int k) {
        this.k = k;
    }

    public void findQuery(String query, Index index, DocumentCorpus corpus, Scanner sc, TokenProcessor mTokenProcessor)
            throws IOException {
        // Treat the query as bag of words in ranked query mode
        List<String> bagOfWords = Arrays.asList(query.split("\\s+"));

        Map<Posting, Double> accumulator = new HashMap<>();

        for (String query_term : bagOfWords) {
            String processedQuery = mTokenProcessor.processQuery(query_term);
            List<Posting> postings = index.getPostingsExcludePositions(processedQuery);

            int N = corpus.getCorpusSize(); // Total number of documents in corpus
            double df_t = postings.size(); // Document frequency of term
            double w_qt = Math.log10(1 + N / df_t); // Weight of term in query

            for (Posting p : postings) {
                double w_dt = 1 + Math.log10(p.getTermFrequency());
                accumulator.put(p, accumulator.getOrDefault(p, 0.0) + (w_dt * w_qt));
            }
        }

        PriorityQueue<Pair> maxHeap = new PriorityQueue<>();

        RandomAccessFile raf = new RandomAccessFile(DocWeightsWriter.getDocWeightFilePath(), "r");
        for (Map.Entry<Posting, Double> entry : accumulator.entrySet()) {
            Posting p = entry.getKey();
            double a_d = entry.getValue();

            int doc_id = p.getDocumentId();
            raf.seek(doc_id * 8); // Seek to the document position to get document weight
            double L_d = raf.readDouble();

            maxHeap.add(new Pair(p, a_d / L_d));
        }
        raf.close();

        // Retrieve top k ranked results from the heap
        List<Posting> topKSearchResultPostings = new ArrayList<>();
        for (int i = 0; i < Math.min(k, maxHeap.size()); i++) {
            Pair top_ith_pair = maxHeap.remove();
            topKSearchResultPostings.add(top_ith_pair.first);
        }

        displaySearchResults(topKSearchResultPostings, corpus, sc);
    }
}
