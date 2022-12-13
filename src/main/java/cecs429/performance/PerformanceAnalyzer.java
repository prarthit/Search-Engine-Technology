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
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.BooleanQuerySearch;
import cecs429.querying.ImpactOrderingSearch;
import cecs429.querying.RankedQuerySearch;
import edu.csulb.EngineStore;
import de.vandermeer.asciitable.AsciiTable;
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
        try (Scanner sc = new Scanner(new File(corpusDir + "/relevance/queries2"))) {
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
            double meanResponseTime = performanceEvaluator.getMeanResponseTime(firstQuery);
            double throughput = performanceEvaluator.getThroughput(meanResponseTime);

            statisticScoresForRankingSchemes
                    .add(new StatisticScores(rankingScoreSchemeName, meanAvgPrecision, meanResponseTime, throughput));

            // plot PR curve for first query
        }

        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow(StatisticScores.getHeader());
        for (StatisticScores statisticScores : statisticScoresForRankingSchemes) {
            at.addRule();
            at.addRow(statisticScores.getContent());
        }
        at.addRule();

        String rend = at.render();
        System.out.println(rend);
    }

    public void analyzeQuery(String query, String rankingMethod) {
        // ranked results and whether each query is relevant or not

        // avg precision

        // throughput for this query
    }

    public void analyzeImpactOrdering(Index index, Index impactIndex, DocumentCorpus corpus) {
        ImpactOrderingSearch impactOrderingSearchEngine = new ImpactOrderingSearch();
        RankedQuerySearch rankedQuerySearchEngineBaseline = new RankedQuerySearch();

        impactOrderingSearchEngine.setK(50);
        rankedQuerySearchEngineBaseline.setK(50);

        List<StatisticScores> statisticScores = new ArrayList<>();

        PerformanceEvaluator performanceEvaluatorImpact = new PerformanceEvaluator(impactIndex, corpus,
                impactOrderingSearchEngine);
        PerformanceEvaluator performanceEvaluatorBaseline = new PerformanceEvaluator(index, corpus,
                rankedQuerySearchEngineBaseline);

        double meanAvgPrecision = performanceEvaluatorImpact.getMeanAvgPrecision(queries, relevantDocNums);
        String firstQuery = queries.get(0);
        double meanResponseTime = performanceEvaluatorImpact.getMeanResponseTime(firstQuery);
        double throughput = performanceEvaluatorImpact.getThroughput(firstQuery);
        double accumulator = performanceEvaluatorImpact.getTotalNonZeroAccumulator(queries);

        double meanAvgPrecisionBaseline = performanceEvaluatorBaseline.getMeanAvgPrecision(queries, relevantDocNums);
        String firstQueryBaseline = queries.get(0);
        double meanResponseTimeBaseline = performanceEvaluatorBaseline.getMeanResponseTime(firstQuery);
        double throughputBaseline = performanceEvaluatorBaseline.getThroughput(firstQueryBaseline);
        double accumulatorBaseline = performanceEvaluatorBaseline.getTotalNonZeroAccumulator(queries);

        System.out.println("\n*** Ranked Queries - Impact Ordering & Baseline ***\n");

        statisticScores
                .add(new StatisticScores("Ranked Impact", meanAvgPrecision, meanResponseTime, throughput));

        statisticScores
                .add(new StatisticScores("Ranked Baseline", meanAvgPrecisionBaseline, meanResponseTimeBaseline,
                        throughputBaseline));

        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow(StatisticScores.getHeader());
        for (StatisticScores ss : statisticScores) {
            at.addRule();
            at.addRow(ss.getContent());
        }
        at.addRule();

        String rend = at.render();
        System.out.println(rend);

        System.out.println("\nBaseline Ranked Accumulator: " + accumulatorBaseline + "\nImpact Ranked Accumulator: "
                + accumulator + "\n");
        // plot PR curve for first query
    }

    public void analyzeImpactOrderingBooleanQueries(Index index, Index impactIndex, DocumentCorpus corpus) {
        BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
        booleanQueryParser.setKGramIndex(EngineStore.getkGramIndex());
        booleanQueryParser.setBiwordIndex(EngineStore.getBiwordIndex());
        booleanQueryParser.setTokenProcessor(EngineStore.getTokenProcessor());

        BooleanQuerySearch booleanQuerySearchEngine = new BooleanQuerySearch(booleanQueryParser);

        List<StatisticScores> statisticScores = new ArrayList<>();

        PerformanceEvaluator performanceEvaluatorImpact = new PerformanceEvaluator(impactIndex, corpus,
                booleanQuerySearchEngine);
        PerformanceEvaluator performanceEvaluatorBaseline = new PerformanceEvaluator(index, corpus,
                booleanQuerySearchEngine);

        double meanAvgPrecision = performanceEvaluatorImpact.getMeanAvgPrecision(queries, relevantDocNums);
        String firstQuery = queries.get(0);
        double meanResponseTime = performanceEvaluatorImpact.getMeanResponseTime(firstQuery);
        double throughput = performanceEvaluatorImpact.getThroughput(firstQuery);

        double meanAvgPrecisionBaseline = performanceEvaluatorBaseline.getMeanAvgPrecision(queries, relevantDocNums);
        String firstQueryBaseline = queries.get(0);
        double meanResponseTimeBaseline = performanceEvaluatorBaseline.getMeanResponseTime(firstQuery);
        double throughputBaseline = performanceEvaluatorBaseline.getThroughput(firstQueryBaseline);

        System.out.println("\n*** Boolean Queries - Impact Ordering & Baseline ***\n");
        statisticScores
                .add(new StatisticScores("Boolean Impact", meanAvgPrecision, meanResponseTime, throughput));

        statisticScores
                .add(new StatisticScores("Boolean Baseline", meanAvgPrecisionBaseline, meanResponseTimeBaseline,
                        throughputBaseline));

        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow(StatisticScores.getHeader());
        for (StatisticScores ss : statisticScores) {
            at.addRule();
            at.addRow(ss.getContent());
        }
        at.addRule();

        String rend = at.render();
        System.out.println(rend);
        // plot PR curve for first query

    }
}
