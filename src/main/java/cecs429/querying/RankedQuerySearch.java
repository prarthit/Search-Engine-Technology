package cecs429.querying;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
import cecs429.text.TokenProcessor;

class Pair implements Comparable<Pair> {
    int first;
    double second;

    Pair(int first, double second) {
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
    private Map<String, VariantStrategy> variantStrategyMap = new HashMap<String, VariantStrategy>() {
        {
            put("default", new DefaultWeightingStrategy());
            put("tf_idf", new Tf_idfWeightingStrategy());
            put("okapi", new OkapiBM25WeightingStrategy());
            put("wacky", new WackyWeightingStrategy());
        }
    };
    private VariantFormulaContext variantFormulaContext = new VariantFormulaContext();
    private WildcardLiteral wildcardLiteral;

    public RankedQuerySearch() {
        variantFormulaContext.setVariantStrategy(new DefaultWeightingStrategy());
    };

    public RankedQuerySearch(int k, String ranking_score_scheme, WildcardLiteral w) {
        this.k = k;
        this.wildcardLiteral = w;

        VariantStrategy variantStrategy = variantStrategyMap.getOrDefault(ranking_score_scheme,
                new DefaultWeightingStrategy());
        variantFormulaContext.setVariantStrategy(variantStrategy);
    }

    private void preFilterBagOfWords(List<String> bagOfWords) {
        ListIterator<String> it = bagOfWords.listIterator();

        while (it.hasNext()) {
            String word = it.next();
            if (word.contains("*")) {
                it.remove();

                wildcardLiteral.setTerm(word);
                for (String newWord : wildcardLiteral.findWordsMatchingWildcard()) {
                    it.add(newWord);
                }
            }
        }
    }

    public void findQuery(String query, Index index, DocumentCorpus corpus, Scanner sc, TokenProcessor mTokenProcessor)
            throws IOException {
        // Treat the query as bag of words in ranked query mode
        List<String> bagOfWords = new ArrayList<>(Arrays.asList(query.split("\\s+")));
        preFilterBagOfWords(bagOfWords);

        Map<Integer, Double> accumulator = new HashMap<>();

        RandomAccessFile raf = new RandomAccessFile(DocWeightsWriter.getDocWeightFilePath(), "r");

        for (String query_term : bagOfWords) {
            String processedQuery = mTokenProcessor.processQuery(query_term);
            List<Posting> postings = index.getPostingsExcludePositions(processedQuery);

            int N = corpus.getCorpusSize(); // Total number of documents in corpus
            double df_t = postings.size(); // Document frequency of term

            double avgDocLength = DocWeightsReader.readAvgDocLength(raf);

            for (Posting p : postings) {
                int docId = p.getDocumentId();
                double tf_td = p.getTermFrequency();

                DocWeights docWeights = DocWeightsReader.readDocWeights(p.getDocumentId(), raf);
                ScoreParameters scoreParameters = variantFormulaContext
                        .executeVariantStrategy(new DocWeightParameters(N, df_t, tf_td, avgDocLength, docWeights));
                double w_dt = scoreParameters.get_w_dt();
                double w_qt = scoreParameters.get_w_qt();

                accumulator.put(docId, accumulator.getOrDefault(docId, 0.0) + (w_dt * w_qt));
            }
        }

        PriorityQueue<Pair> maxHeap = new PriorityQueue<>();

        for (Map.Entry<Integer, Double> entry : accumulator.entrySet()) {
            int docId = entry.getKey();
            double a_d = entry.getValue();

            DocWeights docWeights = DocWeightsReader.readDocWeights(docId, raf);
            ScoreParameters scoreParameters = variantFormulaContext
                    .executeVariantStrategy(new DocWeightParameters(2.0, 2.0, 2.0, 2.0, docWeights));

            double L_d = scoreParameters.get_L_d();

            maxHeap.add(new Pair(docId, a_d / L_d));
        }
        raf.close();

        // Retrieve top k ranked results from the heap
        List<Integer> topKSearchResultsDocIds = new ArrayList<>();
        List<Double> topKAccumulatorValues = new ArrayList<>();
        for (int i = 0; i < Math.min(k, maxHeap.size()); i++) {
            Pair top_ith_pair = maxHeap.remove();
            topKSearchResultsDocIds.add(top_ith_pair.first);

            DecimalFormat df = new DecimalFormat("#.##");
            topKAccumulatorValues.add(Double.parseDouble(df.format(top_ith_pair.second)));
        }

        displaySearchResults(topKSearchResultsDocIds, topKAccumulatorValues, corpus, sc);
    }

    public void findQueryImpactOrder(String query, Index index, DocumentCorpus corpus, Scanner sc,
            TokenProcessor mTokenProcessor)
            throws IOException {
        // Treat the query as bag of words in ranked query mode
        List<String> bagOfWords = new ArrayList<>(Arrays.asList(query.split("\\s+")));
        preFilterBagOfWords(bagOfWords);

        Map<Integer, Double> accumulator = new HashMap<>();

        RandomAccessFile raf = new RandomAccessFile(DocWeightsWriter.getDocWeightFilePath(), "r");

        for (String query_term : bagOfWords) {
            String processedQuery = mTokenProcessor.processQuery(query_term);
            List<Posting> postings = index.getPostingsExcludePositions(processedQuery);

            int N = corpus.getCorpusSize(); // Total number of documents in corpus
            double df_t = postings.size(); // Document frequency of term

            double avgDocLength = DocWeightsReader.readAvgDocLength(raf);

            int impactThresholdValue = 0;

            for (Posting p : postings) {
                impactThresholdValue += p.getTermFrequency();
            }
            impactThresholdValue /= postings.size();

            for (Posting p : postings) {
                if (impactThresholdValue >= p.getTermFrequency()) {
                    break;
                }
                int docId = p.getDocumentId();
                double tf_td = p.getTermFrequency();

                DocWeights docWeights = DocWeightsReader.readDocWeights(p.getDocumentId(), raf);
                ScoreParameters scoreParameters = variantFormulaContext
                        .executeVariantStrategy(new DocWeightParameters(N, df_t, tf_td, avgDocLength, docWeights));
                double w_dt = scoreParameters.get_w_dt();
                double w_qt = scoreParameters.get_w_qt();

                accumulator.put(docId, accumulator.getOrDefault(docId, 0.0) + (w_dt * w_qt));
            }
        }

        PriorityQueue<Pair> maxHeap = new PriorityQueue<>();
        int totalNonZeroAccumulators = 0;

        for (Map.Entry<Integer, Double> entry : accumulator.entrySet()) {
            int docId = entry.getKey();
            double a_d = entry.getValue();
            
            if(a_d > 0){
                ++totalNonZeroAccumulators;
            }

            DocWeights docWeights = DocWeightsReader.readDocWeights(docId, raf);
            ScoreParameters scoreParameters = variantFormulaContext
                    .executeVariantStrategy(new DocWeightParameters(2.0, 2.0, 2.0, 2.0, docWeights));

            double L_d = scoreParameters.get_L_d();

            maxHeap.add(new Pair(docId, a_d / L_d));
        }
        raf.close();

        // Retrieve top k ranked results from the heap
        List<Integer> topKSearchResultsDocIds = new ArrayList<>();
        List<Double> topKAccumulatorValues = new ArrayList<>();
        for (int i = 0; i < Math.min(k, maxHeap.size()); i++) {
            Pair top_ith_pair = maxHeap.remove();
            topKSearchResultsDocIds.add(top_ith_pair.first);

            DecimalFormat df = new DecimalFormat("#.##");
            topKAccumulatorValues.add(Double.parseDouble(df.format(top_ith_pair.second)));
        }

        displaySearchResults(topKSearchResultsDocIds, topKAccumulatorValues, corpus, sc);
    }
}
