package cecs429.indexing.variantFormulas;

public class DocumentWeightingValues {
    private double N;
    private double docWeights;
    private double docLength;
    private double dft;
    private double tftd;
    private double averageTftd;
    private double averageDocLengthCorpus;
    private double byteSize;

    public DocumentWeightingValues(double N,  double docLength, double docWeights, double dft, double tftd, double averageTftd, double averageDocLengthCorpus, double byteSize){
        this.N = N;
        this.tftd = tftd;
        this.dft = dft;
        this.docLength = docLength;
        this.docWeights = docWeights;
        this.averageTftd = averageTftd;
        this.averageDocLengthCorpus = averageDocLengthCorpus;
        this.byteSize = byteSize;
    }

    public double getN() {
        return N;
    }

    public double getDocWeights() {
        return docWeights;
    }

    public double getDocLength() {
        return docLength;
    }

    public double getDft() {
        return dft;
    }

    public double getTftd() {
        return tftd;
    }

    public double getAverageTftd() {
        return averageTftd;
    }

    public double getAverageDocLengthCorpus() {
        return averageDocLengthCorpus;
    }

    public double getByteSize() {
        return byteSize;
    }
}
