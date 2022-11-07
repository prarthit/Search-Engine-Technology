package cecs429.indexing;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

/**
 * Implements a Biword Inverted Index
 */
public class BiwordInvertedIndex implements Index {
	private final HashMap<String, List<Posting>> dict = new HashMap<>();

	/**
	 * Constructs an empty Biword inverted index
	 */
	public BiwordInvertedIndex() {
	}

	public BiwordInvertedIndex(DocumentCorpus corpus, TokenProcessor processor) {
		buildIndexFromCorpus(corpus, processor);
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

	// Clear the index and build a new index from corpus
	public void buildIndexFromCorpus(DocumentCorpus corpus, TokenProcessor processor) {
		long startTime = System.currentTimeMillis(); // Start time to build biword Inverted Index
		System.out.println("Building biword index...");

		dict.clear(); // Clear the index and add terms from new corpus

		// Add terms to the inverted index with addPosting.
		for (Document d : corpus.getDocuments()) {
			Reader content = d.getContent();

			// Tokenize the document's content by constructing an EnglishTokenStream around
			// the document's content.
			EnglishTokenStream englishTokenStream = new EnglishTokenStream(content);

			// Iterate through the tokens in the document, processing them
			// using a BasicTokenProcessor, and adding them to the
			// biword inverted index dictionary.
			Iterator<String> tokens = englishTokenStream.getTokens().iterator();
			List<String> twoConsecutiveTerms = new ArrayList<>();
			while (tokens.hasNext()) {
				List<String> terms = processor.processToken(tokens.next());
				twoConsecutiveTerms.addAll(terms);

				// If there are more than 1 terms in the twoConsectiveTerms list
				// generate all biwords and add them to the biword index
				if (twoConsecutiveTerms.size() > 1) {
					int documentId = d.getId();

					for (String biword : BiwordInvertedIndex.generateBiwords(twoConsecutiveTerms)) {
						addTerm(biword, documentId);
					}

					// If all the biwords have been created, only retain the
					// last term in the twoConsecutiveTerms list in order to pair
					// it with the upcoming next term.
					int len = twoConsecutiveTerms.size();
					String temp = twoConsecutiveTerms.get(len - 1);
					twoConsecutiveTerms.clear();
					twoConsecutiveTerms.add(temp);
				}
			}

			try {
				content.close();
			} catch (IOException e) {
				System.err.println("Unable to close content reader");
			}

			try {
				englishTokenStream.close();
			} catch (IOException e) {
				System.err.println("Unable to close english token stream");
			}
		}

		long endTime = System.currentTimeMillis(); // End time to build biword Inverted Index

		System.out.println(
				"Time taken to build biword inverted index: " + ((endTime - startTime) / 1000)
						+ " seconds");
	}
}
