package cecs429.querying.variantFormulas;

public class WackyWeightingStrategy implements VariantStrategy {

    @Override
    public ScoreParameters variantFormulaExecute(DocWeightParameters docWeightParameters) {
        double w_qt = 0, w_dt = 0, L_d = 0;

        // Calculate the docWeightParameters of wqt, wdt, ld
        w_qt = Math.max(0,
                Math.log((docWeightParameters.getN() - docWeightParameters.get_df_t())
                        / docWeightParameters.get_df_t()));
        w_dt = (1 + Math.log(docWeightParameters.get_tf_td())) / (1 + Math.log(docWeightParameters.getAvg_tf_td()));
        L_d = Math.sqrt(docWeightParameters.getByteSize());

        ScoreParameters scoreParameters = new ScoreParameters(w_dt, w_qt, L_d);
        return scoreParameters;
    }

}
