package cecs429.performance;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.querying.RankedQuerySearch;
import utils.Utils;

public class PerformanceAnalyzer {
    private List<String> queries;
    private List<Set<Integer>> relevantDocNums;

    public PerformanceAnalyzer() {
        queries = parseQueriesFromFile();
        relevantDocNums = parseRelevantDocNums();
    }

    private List<String> parseQueriesFromFile() {
        List<String> queries = new ArrayList<>();
        String corpusDir = Utils.getProperties().getProperty("corpus_directory_path");
        try (Scanner sc = new Scanner(new File(corpusDir + "/relevance/queries"))) {
            while (sc.hasNextLine()) {
                String query = sc.nextLine();
                queries.add(query);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to read the queries");
            e.printStackTrace();
        }

        return queries;
    }

    private Set<Integer> parseRelevantDocNumsStr(String relevantDocNumsStr) {
        Set<Integer> relevantDocNums = new HashSet<>();
        for (String s : relevantDocNumsStr.split(" ")) {
            s.trim();
            if (!s.equalsIgnoreCase(""))
                relevantDocNums.add(Integer.parseInt(s));
        }
        return relevantDocNums;
    }

    private List<Set<Integer>> parseRelevantDocNums() {
        List<Set<Integer>> relevantDocNums = new ArrayList<>();
        String corpusDir = Utils.getProperties().getProperty("corpus_directory_path");
        try (Scanner sc = new Scanner(new File(corpusDir + "/relevance/qrel"))) {
            while (sc.hasNextLine()) {
                String relevantDocNumsStr = sc.nextLine();
                relevantDocNums.add(parseRelevantDocNumsStr(relevantDocNumsStr));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to read the relevant document numbers file");
            e.printStackTrace();
        }

        return relevantDocNums;
    }

    public void analyzeRankingFormulas(Index index, DocumentCorpus corpus) {
        RankedQuerySearch rankedQuerySearchEngine = new RankedQuerySearch();
        List<String> rankingScoreSchemeNames = rankedQuerySearchEngine.getRankingScoreSchemeNames();
        rankedQuerySearchEngine.setK(50);

        List<StatisticScores> statisticScoresForRankingSchemes = new ArrayList<>();

        PerformanceEvaluator performanceEvaluator = new PerformanceEvaluator(index, corpus, rankedQuerySearchEngine);
        for (String rankingScoreSchemeName : rankingScoreSchemeNames) {
            rankedQuerySearchEngine.setRankingScoreScheme(rankingScoreSchemeName);

            double meanAvgPrecision = performanceEvaluator.getMeanAvgPrecision(queries, relevantDocNums);

            String firstQuery = queries.get(0);
            double avgPrecision = performanceEvaluator.getAvgPrecision(firstQuery, relevantDocNums.get(0));
            double meanResponseTime = performanceEvaluator.getMeanResponseTime(firstQuery);
            double throughput = performanceEvaluator.getThroughput(meanResponseTime);

            statisticScoresForRankingSchemes
                    .add(new StatisticScores(rankingScoreSchemeName, meanAvgPrecision, avgPrecision, meanResponseTime,
                            throughput));
        }

        StatisticScores.printScoresList(statisticScoresForRankingSchemes);
    }

    public void analyzeQuery(String query, String rankingMethod) {
        // ranked results and whether each query is relevant or not

        // avg precision

        // throughput for this query
    }
}
