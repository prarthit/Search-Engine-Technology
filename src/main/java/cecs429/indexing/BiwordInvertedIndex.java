package cecs429.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implements a Biword Inverted Index
 */
public class BiwordInvertedIndex extends PositionalInvertedIndex {
	private final HashMap<String, List<Posting>> dict;

	/**
	 * Constructs an empty Biword inverted index
	 */
	public BiwordInvertedIndex() {
		dict = new HashMap<String, List<Posting>>();
	}

	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(String term, int documentId) {
		ArrayList<Posting> postings = (ArrayList<Posting>) dict.computeIfAbsent(term,
				k -> new ArrayList<>());

		Posting lastInsertedPosting = postings.size() != 0 ? postings.get(postings.size() - 1) : null;

		// If the last inserted document id is different, insert it into the postings
		// list
		if (postings.size() == 0 || lastInsertedPosting.getDocumentId() != documentId) {
			postings.add(new Posting(documentId));
		}
	}

}
