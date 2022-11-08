package cecs429.querying.variantFormulas;

public class DocWeightParameters extends DocWeights {
    private double N;
    private double df_t;
    private double tf_td;
    private double avgDocLength;

    public DocWeightParameters(double N, double df_t, double tf_td, double avg_docLength, DocWeights docWeights) {
        super(docWeights.getDocWeight(), docWeights.getDocLength(), docWeights.getByteSize(),
                docWeights.getAvg_tf_td());

        this.N = N;
        this.df_t = df_t;
        this.tf_td = tf_td;
        this.avgDocLength = avg_docLength;
    }

    public double getN() {
        return N;
    }

    public double get_df_t() {
        return df_t;
    }

    public double get_tf_td() {
        return tf_td;
    }

    public double getAvgDocLength() {
        return avgDocLength;
    }
}
