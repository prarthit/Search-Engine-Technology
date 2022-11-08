package cecs429.querying.variantFormulas;

public class DefaultWeightingStrategy implements VariantStrategy {
    @Override
    public ScoreParameters variantFormulaExecute(DocWeightParameters docWeightParameters) {
        double w_dt = 0, w_qt = 0, L_d = 0;

        // Calculate the values of wqt, wdt, ld
        w_qt = Math.log(1 + (docWeightParameters.getN() / docWeightParameters.get_df_t()));
        w_dt = 1 + Math.log(docWeightParameters.get_tf_td());
        L_d = docWeightParameters.getDocWeight();

        ScoreParameters scoreParameters = new ScoreParameters(w_dt, w_qt, L_d);
        return scoreParameters;
    }

}
