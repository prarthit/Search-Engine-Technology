package cecs429.indexing;

/**
 * Implements a Biword class
 */

public class Biword {
	private String firstWord;
	private String secondWord;

	public Biword(String firstWord, String secondWord) {
		this.firstWord = firstWord;
		this.secondWord = secondWord;
	}

	public String getfirstWord() {
		return firstWord;
	}

	public String getSecondWord() {
		return secondWord;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Biword) {
			if (((Biword) o).firstWord.equals(firstWord) && ((Biword) o).secondWord.equals(secondWord)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "[" + firstWord + ", " + secondWord + " ]";
	}
}
