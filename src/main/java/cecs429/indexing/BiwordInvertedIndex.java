package cecs429.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Implements a Positional Inverted Index
 */
public class BiwordInvertedIndex implements Index {
	private final HashMap<Biword, List<Posting>> dict;

	/**
	 * Constructs an empty positional inverted index
	 */
	public BiwordInvertedIndex() {
		dict = new HashMap<Biword, List<Posting>>();
	}

	/**
	 * Associates the given documentId and position with the given term in the
	 * index.
	 */
	public void addTerm(Biword term, int documentId) {
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

	public List<String> getVocabulary() {
		// Convert vocabulary set to list and sort it
		List<Biword> biwordList= new ArrayList<>(dict.keySet());
		List<String> vocabulary = new ArrayList<>();
		for(Biword biword:biwordList){
			vocabulary.add(biword.toString());
		}
		Collections.sort(vocabulary);

		return vocabulary;
	}
}


