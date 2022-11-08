package cecs429.querying.variantFormulas;

public interface VariantStrategy {
    public ScoreParameters variantFormulaExecute(DocWeightParameters documentWeightingValues);
}
