package cecs429.performance;

public class StatisticScores {
    String rankingScoreSchemeName;
    double meanAvgPrecision;
    double meanResponseTime;
    double throughput;

    public StatisticScores(String rankingScoreSchemeName, double meanAvgPrecision, double meanResponseTime,
            double throughput) {
        this.rankingScoreSchemeName = rankingScoreSchemeName;
        this.meanAvgPrecision = meanAvgPrecision;
        this.meanResponseTime = meanResponseTime;
        this.throughput = throughput;
    }
}
