package cecs429.indexing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Implements an Index using a term-document matrix. Requires knowing the full
 * corpus vocabulary and number of documents
 * prior to construction.
 */
public class TermDocumentIndex implements Index {
	private final HashMap<String, List<Integer>> dict;

	/**
	 * Constructs an empty inverted index and empty vocabulary set
	 */
	public TermDocumentIndex() {
		dict = new HashMap<String, List<Integer>>();
	}

	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(String term, int documentId) {
		ArrayList<Integer> documentIds = (ArrayList<Integer>) dict.computeIfAbsent(term,
				k -> new ArrayList<Integer>(Arrays.asList(documentId)));

		// If the last inserted document id is same don't insert it into the list
		if (documentIds.get(documentIds.size() - 1) != documentId) {
			documentIds.add(documentId);
		}
	}

	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> postings = new ArrayList<>();

		// Get the document IDs for the term from inverted index dictionary
		ArrayList<Integer> documentIds = (ArrayList<Integer>) dict.getOrDefault(term, new ArrayList<Integer>());
		documentIds.forEach(documentId -> {
			postings.add(new Posting(documentId));
		});

		return postings;
	}

	public List<String> getVocabulary() {
		// Convert sorted vocabulary set to list
		List<String> vocabulary = new ArrayList<>(dict.keySet());
		Collections.sort(vocabulary);

		return vocabulary;
	}
}
