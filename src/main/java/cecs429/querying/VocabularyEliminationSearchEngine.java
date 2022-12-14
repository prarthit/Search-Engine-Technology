package cecs429.querying;

import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import cecs429.indexing.Posting;
import cecs429.querying.variantFormulas.DocWeightParameters;
import cecs429.querying.variantFormulas.DocWeights;
import cecs429.querying.variantFormulas.DocWeightsReader;
import cecs429.querying.variantFormulas.ScoreParameters;
import cecs429.querying.variantFormulas.VariantFormulaContext;

public class VocabularyEliminationSearchEngine extends RankedQuerySearch {
    private float wqtThreshhold;

    public VocabularyEliminationSearchEngine(float wqtThreshhold) {
        super();
        this.wqtThreshhold = wqtThreshhold;
    }

    public void setWQTThreshold(float wqtThreshhold){
        this.wqtThreshhold = wqtThreshhold;
    }

    @Override
    protected void computeAccumulator(List<Posting> postings, RandomAccessFile raf,
            VariantFormulaContext variantFormulaContext, int N, double avgDocLength, Map<Integer, Double> accumulator,
            double impactThresholdValue) {

        double df_t = postings.size(); // Document frequency of term

        for (Posting p : postings) {

            int docId = p.getDocumentId();
            double tf_td = p.getTermFrequency();

            DocWeights docWeights = DocWeightsReader.readDocWeights(p.getDocumentId(), raf);
            ScoreParameters scoreParameters = variantFormulaContext
                    .executeVariantStrategy(new DocWeightParameters(N, df_t, tf_td, avgDocLength, docWeights));
            double w_dt = scoreParameters.get_w_dt();
            double w_qt = scoreParameters.get_w_qt();
            if(w_qt >= wqtThreshhold){
                accumulator.put(docId, accumulator.getOrDefault(docId, 0.0) + (w_dt * w_qt));
            }
        }
    }
}
