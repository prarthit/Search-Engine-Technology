package cecs429.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Implements a Positional Inverted Index
 */
public class PositionalInvertedIndex implements Index {
	private final HashMap<String, List<Posting>> dict;

	/**
	 * Constructs an empty positional inverted index
	 */
	public PositionalInvertedIndex() {
		dict = new HashMap<String, List<Posting>>();
	}

	/**
	 * Associates the given documentId and position with the given term in the
	 * index.
	 */
	public void addTerm(String term, int documentId, int position) {
		ArrayList<Posting> postings = (ArrayList<Posting>) dict.computeIfAbsent(term,
				k -> new ArrayList<>());

		Posting lastInsertedPosting = postings.size() != 0 ? postings.get(postings.size() - 1) : null;

		// If the last inserted document id is different, insert it into the postings
		// list
		if (postings.size() == 0 || lastInsertedPosting.getDocumentId() != documentId) {
			postings.add(new Posting(documentId, position));
		} else {
			// For same document id, insert the new position of the term
			lastInsertedPosting.addPosition(position);
		}
	}

	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> postings = (List<Posting>) dict.getOrDefault(term, new ArrayList<Posting>());
		return postings;
	}

	@Override
	public List<Posting> getPostingsExcludePositions(String term) {
		List<Posting> postings = (List<Posting>) dict.getOrDefault(term, new ArrayList<Posting>());
		return postings;
	}

	public List<String> getVocabulary() {
		// Convert vocabulary set to list and sort it
		List<String> vocabulary = new ArrayList<>(dict.keySet());
		Collections.sort(vocabulary);

		return vocabulary;
	}
}
