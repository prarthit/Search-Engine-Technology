package cecs429.indexing;

/**
 * Implements a Biword class
 */

public class Biword {
	private String firstTerm;
	private String secondTerm;

	public Biword(String firstTerm, String secondTerm) {
		this.firstTerm = firstTerm;
		this.secondTerm = secondTerm;
	}

	public String getfirstTerm() {
		return firstTerm;
	}

	public String getSecondTerm() {
		return secondTerm;
	}

	@Override
	public String toString() {
		return "[" + firstTerm + "," + secondTerm + "]";
	}
}
