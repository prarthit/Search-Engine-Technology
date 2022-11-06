package cecs429.indexing.variantFormulas;

public class TfidfWeightingStrategy implements VariantStrategy {

    @Override
    public WeightingParameters variantFormulaExecute(DocumentWeightingValues values) {
        double wqt = 0, wdt = 0, ld = 0;

        // Calculate the values of wqt, wdt, ld
        wqt = Math.log((values.getN() / values.getDft()));
        wdt = values.getTftd();
        ld = values.getDocWeights();

        WeightingParameters weightingParameters = new WeightingParameters();
        weightingParameters.setWeightingValues(wdt, wqt, ld);
        return weightingParameters;
    }

}
