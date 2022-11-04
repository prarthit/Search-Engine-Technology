package cecs429.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Implements a Biword Inverted Index
 */
public class BiwordInvertedIndex implements Index {
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

	// Generate all biwords from a list of words
	public static List<String> generateBiwords(List<String> words) {
		List<String> biwords = new ArrayList<String>();

		for (int i = 0; i < words.size() - 1; i++) {
			String word1 = words.get(i);
			String word2 = words.get(i + 1);

			String biword = word1 + " " + word2;
			biwords.add(biword);
		}

		return biwords;
	}
}
