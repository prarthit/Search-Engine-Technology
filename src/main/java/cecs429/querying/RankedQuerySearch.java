package cecs429.querying;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
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
import cecs429.querying.variantFormulas.DefaultWeightingStrategy;
import cecs429.querying.variantFormulas.DocWeightParameters;
import cecs429.querying.variantFormulas.DocWeights;
import cecs429.querying.variantFormulas.DocWeightsReader;
import cecs429.querying.variantFormulas.DocWeightsWriter;
import cecs429.querying.variantFormulas.OkapiBM25WeightingStrategy;
import cecs429.querying.variantFormulas.ScoreParameters;
import cecs429.querying.variantFormulas.Tf_idfWeightingStrategy;
import cecs429.querying.variantFormulas.VariantFormulaContext;
import cecs429.querying.variantFormulas.VariantStrategy;
import cecs429.querying.variantFormulas.WackyWeightingStrategy;

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
    private VariantFormulaContext variantFormulaContext = new VariantFormulaContext();

    public RankedQuerySearch() {
        variantFormulaContext.setVariantStrategy(new DefaultWeightingStrategy());
    };

    public RankedQuerySearch(int k, String ranking_score_scheme) {
        this.k = k;

        VariantStrategy variantStrategy = getVariantStrategy(ranking_score_scheme);
        variantFormulaContext.setVariantStrategy(variantStrategy);
    }

    private VariantStrategy getVariantStrategy(String ranking_score_scheme) {
        if (ranking_score_scheme.equals("tf_idf"))
            return new Tf_idfWeightingStrategy();
        else if (ranking_score_scheme.equals("okapi"))
            return new OkapiBM25WeightingStrategy();
        else if (ranking_score_scheme.equals("wacky"))
            return new WackyWeightingStrategy();
        else
            return new DefaultWeightingStrategy();
    }

    public void findQuery(String query, Index index, DocumentCorpus corpus, Scanner sc)
            throws IOException {
        // Treat the query as bag of words in ranked query mode
        List<String> bagOfWords = Arrays.asList(query.split("\\s+"));

        Map<Posting, Double> accumulator = new HashMap<>();

        RandomAccessFile raf = new RandomAccessFile(DocWeightsWriter.getDocWeightFilePath(), "r");

        for (String query_term : bagOfWords) {
            List<Posting> postings = index.getPostingsExcludePositions(query_term);

            int N = corpus.getCorpusSize(); // Total number of documents in corpus
            double df_t = postings.size(); // Document frequency of term

            double avgDocLength = DocWeightsReader.readAvgDocLength(raf);

            for (Posting p : postings) {
                double tf_td = p.getTermFrequency();

                DocWeights docWeights = DocWeightsReader.readDocWeights(p.getDocumentId(), raf);
                ScoreParameters scoreParameters = variantFormulaContext
                        .executeVariantStrategy(new DocWeightParameters(N, df_t, tf_td, avgDocLength, docWeights));
                double w_dt = scoreParameters.get_w_dt();
                double w_qt = scoreParameters.get_w_qt();

                accumulator.put(p, accumulator.getOrDefault(p, 0.0) + (w_dt * w_qt));
            }
        }

        PriorityQueue<Pair> maxHeap = new PriorityQueue<>();

        for (Map.Entry<Posting, Double> entry : accumulator.entrySet()) {
            Posting p = entry.getKey();
            double a_d = entry.getValue();

            DocWeights docWeights = DocWeightsReader.readDocWeights(p.getDocumentId(), raf);
            ScoreParameters scoreParameters = variantFormulaContext
                    .executeVariantStrategy(new DocWeightParameters(2.0, 2.0, 2.0, 2.0, docWeights));

            double L_d = scoreParameters.get_L_d();

            maxHeap.add(new Pair(p, a_d / L_d));
        }
        raf.close();

        // Retrieve top k ranked results from the heap
        List<Posting> topKSearchResultPostings = new ArrayList<>();
        List<Double> topKAccumulatorValues = new ArrayList<>();
        for (int i = 0; i < Math.min(k, maxHeap.size()); i++) {
            Pair top_ith_pair = maxHeap.remove();
            topKSearchResultPostings.add(top_ith_pair.first);

            DecimalFormat df = new DecimalFormat("#.##");
            topKAccumulatorValues.add(Double.parseDouble(df.format(top_ith_pair.second)));
        }

        displaySearchResults(topKSearchResultPostings, topKAccumulatorValues, corpus, sc);
    }
}
