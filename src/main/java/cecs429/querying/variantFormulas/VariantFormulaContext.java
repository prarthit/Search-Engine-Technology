package cecs429.querying.variantFormulas;

public class VariantFormulaContext {
    private VariantStrategy variantStrategy;

    public void setVariantStrategy(VariantStrategy variantStrategy) {
        this.variantStrategy = variantStrategy;
    }

    public ScoreParameters executeVariantStrategy(DocWeightParameters docWeightParameters) {
        return variantStrategy.variantFormulaExecute(docWeightParameters);
    }
}
