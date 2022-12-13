package cecs429.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.querying.QuerySearch;
import cecs429.querying.RankedQuerySearch;
import cecs429.querying.Result;

public class PerformanceEvaluator {
    private Index index;
    private DocumentCorpus corpus;
    private QuerySearch querySearchEngine;

    public PerformanceEvaluator(Index index, DocumentCorpus corpus, QuerySearch querySearchEngine) {
        this.index = index;
        this.corpus = corpus;
        this.querySearchEngine = querySearchEngine;
    }

    public double getAvgPrecision(String query, Set<Integer> relevantDocNums) {
        List<Result> results = querySearchEngine.findQuery(query, index, corpus);

        return getAvgPrecision(results, relevantDocNums);
    }

    private double getAvgPrecision(List<Result> results, Set<Integer> relevantDocNums) {
        List<Integer> resultDocNums = new ArrayList<>();
        for (Result result : results) {
            resultDocNums.add(result.getDocNum());
        }

        double sumOfPrecision = 0.0;
        int rel_size = relevantDocNums.size(); // Size of relevant document ids

        // Total number of relevant documents from results if we consider only first i
        // results
        int total_relevant_till_i = 0;
        for (int i = 1; i <= resultDocNums.size(); i++) {
            int currDocNum = resultDocNums.get(i - 1);

            // relevant(i) - Is the current docId relevant?
            int isRelevant = relevantDocNums.contains(currDocNum) ? 1 : 0;
            total_relevant_till_i = isRelevant == 1 ? total_relevant_till_i + 1 : total_relevant_till_i;
            double P_i = (double) total_relevant_till_i / i; // P@i - Precision if we consider only first i results

            sumOfPrecision += isRelevant * P_i;
        }

        return sumOfPrecision / rel_size;
    }

    public double getMeanAvgPrecision(List<String> queries, List<Set<Integer>> relevantDocNums) {
        double sumOfAvgPrecisions = 0.0;
        int totalQueries = queries.size();

        for (int i = 0; i < totalQueries; i++) {
            String query = queries.get(i);
            Set<Integer> relevantDocNumsForQuery = relevantDocNums.get(i);

            sumOfAvgPrecisions += getAvgPrecision(query, relevantDocNumsForQuery);
        }

        return sumOfAvgPrecisions / totalQueries;
    }

    public double getMeanResponseTime(String query) {
        double totalTime = 0;
        int iterations = 1;

        // Run findQuery for num of iterations and take the average of the response time
        for (int i = 1; i <= iterations; i++) {
            long startTime = System.currentTimeMillis(); // Start time to find results for the query
            querySearchEngine.findQuery(query, index, corpus);
            long endTime = System.currentTimeMillis(); // End time to find results for the query

            double timeTakenToFindResults = (endTime - startTime);
            totalTime += timeTakenToFindResults;
        }

        return totalTime / iterations;
    }

    public double getThroughput(String query) {
        return getThroughput(getMeanResponseTime(query));
    }

    public double getThroughput(double mrt) {
        return 1000 / mrt;
    }

    public int getTotalNonZeroAccumulator(){
        return ((RankedQuerySearch)querySearchEngine).getTotalNonZeroAccumulator();
    }
}
