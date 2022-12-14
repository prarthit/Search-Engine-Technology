package cecs429.performance;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.BooleanQuerySearch;
import cecs429.querying.ImpactOrderingSearch;
import cecs429.querying.RankedQuerySearch;
import cecs429.querying.VocabularyEliminationSearchEngine;
import edu.csulb.EngineStore;
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

            performanceEvaluator.drawPRCurve(firstQuery, rankingScoreSchemeName, relevantDocNums.get(0));
        }

        System.out.println("\n*** Ranked Queries - Variant Strategy Formula ***\n");
        StatisticScores.printScoresList(statisticScoresForRankingSchemes);
    }

    public void analyzeVocabElimination(Index index, DocumentCorpus corpus) {
        RankedQuerySearch rankedQuerySearchEngine = new RankedQuerySearch();
        rankedQuerySearchEngine.setK(50);
        List<Float> wqtList = Arrays.asList(1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.7f, 1.9f, 2.1f, 2.3f, 2.5f);
        List<StatisticScores> statisticScoresForRankingSchemes = new ArrayList<>();

        for (float wqt : wqtList) {
            VocabularyEliminationSearchEngine vocabEliminationSearchEngine = new VocabularyEliminationSearchEngine(wqt);

            PerformanceEvaluator performanceEvaluator = new PerformanceEvaluator(index, corpus,
                    vocabEliminationSearchEngine);

            double meanAvgPrecision = performanceEvaluator.getMeanAvgPrecision(queries, relevantDocNums);
            String firstQuery = queries.get(0);
            double avgPrecision = performanceEvaluator.getAvgPrecision(firstQuery, relevantDocNums.get(0));
            double meanResponseTime = performanceEvaluator.getMeanResponseTime(firstQuery);
            double throughput = performanceEvaluator.getThroughput(meanResponseTime);

            statisticScoresForRankingSchemes
                    .add(new StatisticScores("Vocab Elimination", meanAvgPrecision, avgPrecision, meanResponseTime,
                            throughput));
        }
        System.out.println("\n*** Ranked Queries - Vocab Elimination ***\n");
        StatisticScores.printScoresList(statisticScoresForRankingSchemes);
    }

    public void analyzeImpactOrdering(Index index, Index impactIndex, DocumentCorpus corpus) {
        ImpactOrderingSearch impactOrderingSearchEngine = new ImpactOrderingSearch();
        RankedQuerySearch rankedQuerySearchEngineBaseline = new RankedQuerySearch();

        impactOrderingSearchEngine.setK(50);
        rankedQuerySearchEngineBaseline.setK(50);

        List<StatisticScores> statisticScoresForRankingSchemes = new ArrayList<>();

        PerformanceEvaluator performanceEvaluatorImpact = new PerformanceEvaluator(impactIndex, corpus,
                impactOrderingSearchEngine);
        PerformanceEvaluator performanceEvaluatorBaseline = new PerformanceEvaluator(index, corpus,
                rankedQuerySearchEngineBaseline);

        double meanAvgPrecision = performanceEvaluatorImpact.getMeanAvgPrecision(queries, relevantDocNums);
        String firstQuery = queries.get(0);
        double avgPrecision = performanceEvaluatorImpact.getAvgPrecision(firstQuery, relevantDocNums.get(0));
        double meanResponseTime = performanceEvaluatorImpact.getMeanResponseTime(firstQuery);
        double throughput = performanceEvaluatorImpact.getThroughput(firstQuery);
        double accumulator = performanceEvaluatorImpact.getTotalNonZeroAccumulator(queries);

        double meanAvgPrecisionBaseline = performanceEvaluatorBaseline.getMeanAvgPrecision(queries, relevantDocNums);
        String firstQueryBaseline = queries.get(0);
        double avgPrecisionBaseline = performanceEvaluatorBaseline.getAvgPrecision(firstQuery, relevantDocNums.get(0));
        double meanResponseTimeBaseline = performanceEvaluatorBaseline.getMeanResponseTime(firstQuery);
        double throughputBaseline = performanceEvaluatorBaseline.getThroughput(firstQueryBaseline);
        double accumulatorBaseline = performanceEvaluatorBaseline.getTotalNonZeroAccumulator(queries);

        System.out.println("\n*** Ranked Queries - Impact Ordering & Baseline ***\n");

        statisticScoresForRankingSchemes
                .add(new StatisticScores("Ranked Impact", meanAvgPrecision, avgPrecision, meanResponseTime,
                        throughput));

        statisticScoresForRankingSchemes
                .add(new StatisticScores("Ranked Baseline", meanAvgPrecisionBaseline, avgPrecisionBaseline,
                        meanResponseTimeBaseline,
                        throughputBaseline));

        StatisticScores.printScoresList(statisticScoresForRankingSchemes);

        System.out.println("\nBaseline Ranked Accumulator: " + accumulatorBaseline + "\nImpact Ranked Accumulator: "
                + accumulator + "\n");

        performanceEvaluatorImpact.drawPRCurve(firstQuery, "Ranked Impact", relevantDocNums.get(0));
        performanceEvaluatorBaseline.drawPRCurve(firstQuery, "Ranked Baseline", relevantDocNums.get(0));
    }

    public void analyzeImpactOrderingBooleanQueries(Index index, Index impactIndex, DocumentCorpus corpus) {
        BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
        booleanQueryParser.setKGramIndex(EngineStore.getkGramIndex());
        booleanQueryParser.setBiwordIndex(EngineStore.getBiwordIndex());
        booleanQueryParser.setTokenProcessor(EngineStore.getTokenProcessor());

        BooleanQuerySearch booleanQuerySearchEngine = new BooleanQuerySearch(booleanQueryParser);

        List<StatisticScores> statisticScoresForRankingSchemes = new ArrayList<>();

        PerformanceEvaluator performanceEvaluatorImpact = new PerformanceEvaluator(impactIndex, corpus,
                booleanQuerySearchEngine);
        PerformanceEvaluator performanceEvaluatorBaseline = new PerformanceEvaluator(index, corpus,
                booleanQuerySearchEngine);

        double meanAvgPrecision = performanceEvaluatorImpact.getMeanAvgPrecision(queries, relevantDocNums);
        String firstQuery = queries.get(0);
        double avgPrecision = performanceEvaluatorBaseline.getAvgPrecision(firstQuery, relevantDocNums.get(0));
        double meanResponseTime = performanceEvaluatorImpact.getMeanResponseTime(firstQuery);
        double throughput = performanceEvaluatorImpact.getThroughput(firstQuery);

        double meanAvgPrecisionBaseline = performanceEvaluatorBaseline.getMeanAvgPrecision(queries, relevantDocNums);
        String firstQueryBaseline = queries.get(0);
        double avgPrecisionBaseline = performanceEvaluatorBaseline.getAvgPrecision(firstQuery, relevantDocNums.get(0));
        double meanResponseTimeBaseline = performanceEvaluatorBaseline.getMeanResponseTime(firstQuery);
        double throughputBaseline = performanceEvaluatorBaseline.getThroughput(firstQueryBaseline);

        System.out.println("\n*** Boolean Queries - Impact Ordering & Baseline ***\n");
        statisticScoresForRankingSchemes
                .add(new StatisticScores("Boolean Impact", meanAvgPrecision, avgPrecision, meanResponseTime,
                        throughput));

        statisticScoresForRankingSchemes
                .add(new StatisticScores("Boolean Baseline", meanAvgPrecisionBaseline, avgPrecisionBaseline,
                        meanResponseTimeBaseline,
                        throughputBaseline));

        StatisticScores.printScoresList(statisticScoresForRankingSchemes);
    }
}
