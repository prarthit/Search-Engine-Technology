package cecs429.indexing.variantFormulas;

public class WackyWeightingStrategy implements VariantStrategy {

    @Override
    public WeightingParameters variantFormulaExecute(DocumentWeightingValues values) {
        double wqt = 0, wdt = 0, ld = 0;

        // Calculate the values of wqt, wdt, ld
        wqt = Math.max(0, Math.log((values.getN() - values.getDft()) / values.getDft()));
        wdt = (1 + Math.log(values.getTftd())) / (1 + Math.log(values.getAverageTftd()));
        ld = Math.sqrt(values.getByteSize());

        WeightingParameters weightingParameters = new WeightingParameters();
        weightingParameters.setWeightingValues(wdt, wqt, ld);
        return weightingParameters;
    }

}
