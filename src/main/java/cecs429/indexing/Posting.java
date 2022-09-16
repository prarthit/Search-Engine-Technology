package cecs429.indexing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPositions; // Positions of term in document

	public Posting(int documentId, int position) {
		mDocumentId = documentId;
		mPositions = new ArrayList<>(Arrays.asList(position));
	}

	// Add position of term to mPositions list
	public void addPosition(int position) {
		mPositions.add(position);
	}

	public int getDocumentId() {
		return mDocumentId;
	}
}
