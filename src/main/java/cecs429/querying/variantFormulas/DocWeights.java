package cecs429.querying.variantFormulas;

import java.util.Map;

public class DocWeights {
    protected double docWeight;
    protected int docLength;
    protected long byteSize;
    protected double avg_tf_td;

    public double getDocWeight() {
        return docWeight;
    }

    public int getDocLength() {
        return docLength;
    }

    public long getByteSize() {
        return byteSize;
    }

    public double getAvg_tf_td() {
        return avg_tf_td;
    }

    public static int getClassByteSize() {
        // Sum total bytes of datatype for each attribute
        return 8 + 4 + 8 + 8;
    }

    public DocWeights(double docWeight, int docLength, long byteSize, double ave_tf_td) {
        this.docWeight = docWeight;
        this.docLength = docLength;
        this.byteSize = byteSize;
        this.avg_tf_td = ave_tf_td;
    }

    public DocWeights(Map<String, Integer> termFreqMap, int docLength, long byteSize) {
        this.docWeight = calculateDocWeight(termFreqMap);
        this.docLength = docLength;
        this.byteSize = byteSize;
        this.avg_tf_td = calculateAveTermFreq(termFreqMap);
    }

    private double calculateAveTermFreq(Map<String, Integer> termFreqMap) {
        // Sum of term frequencies
        int sum_of_tf = termFreqMap.values().stream().reduce(0, Integer::sum);
        double total_terms = termFreqMap.size();

        // Average term frequency for document
        double ave_tf_td = sum_of_tf / total_terms;
        return ave_tf_td;
    }

    private double calculateDocWeight(Map<String, Integer> termFreqMap) {
        // Sum of term weights
        double sum_of_w_dt = 0;

        for (int freq : termFreqMap.values()) {
            double w_dt = 1 + Math.log10(freq); // Weight of a term in a document
            sum_of_w_dt = w_dt * w_dt;
        }

        // Weight of document
        double L_d = Math.sqrt(sum_of_w_dt); // Normalize sum of term weights

        return L_d;
    }
}
