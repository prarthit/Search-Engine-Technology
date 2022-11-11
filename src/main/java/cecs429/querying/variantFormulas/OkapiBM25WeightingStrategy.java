package cecs429.querying.variantFormulas;

public class OkapiBM25WeightingStrategy implements VariantStrategy {

    @Override
    public ScoreParameters variantFormulaExecute(DocWeightParameters docWeightParameters) {
        double w_qt = 0, w_dt = 0, L_d = 0;

        // Calculate the docWeightParameters of wqt, wdt, ld
        w_qt = Math.max(0.1, Math.log((docWeightParameters.getN() - docWeightParameters.get_df_t() + 0.5)
                / (docWeightParameters.get_df_t() + 0.5)));
        w_dt = 2.2 * docWeightParameters.get_tf_td()
                / (1.2 * (0.25 + (0.75 * (docWeightParameters.getDocLength() / docWeightParameters.getAvgDocLength())))
                        + docWeightParameters.get_tf_td());
        L_d = 1;

        ScoreParameters scoreParameters = new ScoreParameters(w_dt, w_qt, L_d);
        return scoreParameters;
    }

}
