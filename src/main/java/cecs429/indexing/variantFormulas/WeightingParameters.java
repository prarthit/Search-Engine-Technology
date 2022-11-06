package cecs429.indexing.variantFormulas;

public class WeightingParameters {
    private double wdt;
    private double wqt;
    private double ld;

    public void setWeightingValues(double wdt, double wqt, double ld) {
        this.wdt = wdt;
        this.wqt = wqt;
        this.ld = ld;
    }

    public double getWdt() {
        return wdt;
    }

    public double getWqt() {
        return wqt;
    }

    public double getld() {
        return ld;
    }
}
