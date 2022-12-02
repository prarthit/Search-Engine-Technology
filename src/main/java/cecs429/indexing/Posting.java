package cecs429.indexing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting implements Comparable {
	private int mDocumentId;
	private int mTermFrequency;
	private List<Integer> mPositions; // Positions of term in document

	public int compareTo(Object obj) {
		Posting mPositions2 = (Posting) obj;

		if (mPositions.size() < mPositions2.getPositions().size()) {
			return 1;
		}
		if (mPositions.size() > mPositions2.getPositions().size()) {
			return -1;
		}

		return 0;
	}

	public Posting(int documentId) {
		mDocumentId = documentId;
		mPositions = new ArrayList<>();
	}

	public Posting(int documentId, int position) {
		mDocumentId = documentId;
		mPositions = new ArrayList<>(Arrays.asList(position));
	}

	public Posting(int documentId, List<Integer> positions) {
		mDocumentId = documentId;
		mPositions = positions;
	}

	// Add position of term to mPositions list
	public void addPosition(int position) {
		mPositions.add(position);
	}

	public List<Integer> getPositions() {
		return mPositions;
	}

	public int getDocumentId() {
		return mDocumentId;
	}

	public int getTermFrequency() {
		return mTermFrequency;
	}

	public void setTermFrequency(int tf) {
		mTermFrequency = tf;
	}
}
