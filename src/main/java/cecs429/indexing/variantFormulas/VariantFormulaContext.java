package cecs429.indexing.variantFormulas;

public class VariantFormulaContext {
    private VariantStrategy variantStrategy;

    public void setVariantStrategy(VariantStrategy variantStrategy){
        this.variantStrategy = variantStrategy;
    }

    public WeightingParameters executeVariantStrategy(DocumentWeightingValues documentWeightingValues){
        return variantStrategy.variantFormulaExecute(documentWeightingValues);
    }
}
