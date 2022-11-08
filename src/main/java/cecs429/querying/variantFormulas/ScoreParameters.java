package cecs429.querying.variantFormulas;

public class ScoreParameters {
    private double w_dt; // Weight of term in document
    private double w_qt; // Weight of term in query
    private double L_d; // Euclidian weight of document

    public ScoreParameters(double w_dt, double w_qt, double L_d) {
        this.w_dt = w_dt;
        this.w_qt = w_qt;
        this.L_d = L_d;
    }

    public double get_w_dt() {
        return w_dt;
    }

    public double get_w_qt() {
        return w_qt;
    }

    public double get_L_d() {
        return L_d;
    }
}
