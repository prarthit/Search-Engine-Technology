package cecs429.indexing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private long mTermFrequency;
	private List<Integer> mPositions; // Positions of term in document

	public Posting(int documentId) {
		mDocumentId = documentId;
		mPositions = new ArrayList<>();
	}

	public Posting(int documentId, int position) {
		mDocumentId = documentId;
		mPositions = new ArrayList<>(Arrays.asList(position));
	}

	public Posting(int documentId, List<Integer>  positions) {
		mDocumentId = documentId;
		mPositions = positions;
	}

	public Posting(int documentId, long termFrequency) {
		mDocumentId = documentId;
		mTermFrequency = termFrequency;
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

	public long getTermFrequency() {
		return mTermFrequency;
	}
}
