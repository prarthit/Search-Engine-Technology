package cecs429.performance;

import java.util.List;

import de.vandermeer.asciitable.AT_Cell;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import utils.Utils;

public class StatisticScores {
    String scoreName;
    double meanAvgPrecision;
    double avgPrecisionForSingleQuery;
    double meanResponseTimeForSingleQuery;
    double throughputForSingleQuery;

    public StatisticScores(String scoreName, double meanAvgPrecision, double avgPrecisionForSingleQuery,
            double meanResponseTimeForSingleQuery,
            double throughputForSingleQuery) {
        this.scoreName = scoreName;
        this.meanAvgPrecision = Utils.formatDouble(meanAvgPrecision);
        this.avgPrecisionForSingleQuery = Utils.formatDouble(avgPrecisionForSingleQuery);
        this.meanResponseTimeForSingleQuery = Utils.formatDouble(meanResponseTimeForSingleQuery);
        this.throughputForSingleQuery = Utils.formatDouble(throughputForSingleQuery);
    }

    private Object[] getContent() {
        return new Object[] { scoreName, meanAvgPrecision, avgPrecisionForSingleQuery, meanResponseTimeForSingleQuery,
                throughputForSingleQuery };
    }

    public static void printScoresList(List<StatisticScores> statisticScoresForItem) {
        AsciiTable at = new AsciiTable();
        at.addRule();

        AT_Cell cell = at.addRow("", "", null, null, "1st Query").getCells().getLast();
        cell.getContext().setTextAlignment(TextAlignment.CENTER);
        at.addRule();

        at.addRow("ScoreName", "MAP(cranfield all Queries)", "Avg Precision", "Mean response time(in ms)",
                "Throughput");

        for (StatisticScores statisticScores : statisticScoresForItem) {
            at.addRule();
            at.addRow(statisticScores.getContent());
        }
        at.addRule();

        String rend = at.render();
        System.out.println(rend);
    }
}
