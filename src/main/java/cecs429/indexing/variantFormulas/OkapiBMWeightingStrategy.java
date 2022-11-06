package cecs429.indexing.variantFormulas;

public class OkapiBMWeightingStrategy implements VariantStrategy {

    @Override
    public WeightingParameters variantFormulaExecute(DocumentWeightingValues values) {
        double wqt = 0, wdt = 0, ld = 0;

        // Calculate the values of wqt, wdt, ld
        wqt = Math.max(0.1, Math.log((values.getN() - values.getDft() + 0.5) / (values.getDft() + 0.5)));
        wdt = 2.2 * values.getTftd()
                / (1.2 * (0.25 + (0.75 * (values.getDocLength() / values.getAverageDocLengthCorpus())))
                        + values.getTftd());
        ld = values.getDocWeights();

        WeightingParameters weightingParameters = new WeightingParameters();
        weightingParameters.setWeightingValues(wdt, wqt, ld);
        return weightingParameters;
    }

}
