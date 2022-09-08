package cecs429.indexing;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	
	public Posting(int documentId) {
		mDocumentId = documentId;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
}
