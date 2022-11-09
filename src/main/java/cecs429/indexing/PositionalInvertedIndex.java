package cecs429.indexing;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.querying.variantFormulas.DocWeights;
import cecs429.querying.variantFormulas.DocWeightsWriter;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

/**
 * Implements a Positional Inverted Index
 */
public class PositionalInvertedIndex implements Index {
	private final HashMap<String, List<Posting>> dict = new HashMap<>();

	/**
	 * Constructs an empty positional inverted index
	 */
	public PositionalInvertedIndex() {
	}

	public PositionalInvertedIndex(DocumentCorpus corpus, TokenProcessor processor) {
		buildIndexFromCorpus(corpus, processor);
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

		for (Posting p : postings) {
			p.setTermFrequency(p.getPositions().size());
		}

		return postings;
	}

	public List<String> getVocabulary() {
		// Convert vocabulary set to list and sort it
		List<String> vocabulary = new ArrayList<>(dict.keySet());
		Collections.sort(vocabulary);

		return vocabulary;
	}

	// Clear the index and build a new index from corpus
	public void buildIndexFromCorpus(DocumentCorpus corpus, TokenProcessor processor) {
		long startTime = System.currentTimeMillis(); // Start time to build positional Inverted Index
		System.out.println("Building positional inverted index...");

		dict.clear(); // Clear the index and add terms from new corpus

		List<DocWeights> docWeightsList = new ArrayList<>();

		// Add terms to the inverted index with addPosting.
		for (Document d : corpus.getDocuments()) {
			Reader content = d.getContent();

			// Term frequency map for count of term in a document
			Map<String, Integer> termFreqMap = new HashMap<>();

			// Tokenize the document's content by constructing an EnglishTokenStream around
			// the document's content.
			EnglishTokenStream englishTokenStream = new EnglishTokenStream(content);

			// Iterate through the tokens in the document, processing them
			// using a BasicTokenProcessor, and adding them to the
			// positional inverted index dictionary.
			Iterator<String> tokens = englishTokenStream.getTokens().iterator();
			int position = 0; // Position of term in document
			while (tokens.hasNext()) {
				List<String> terms = processor.processToken(tokens.next());
				int documentId = d.getId();
				for (String term : terms) {
					addTerm(term, documentId, position);

					termFreqMap.put(term, termFreqMap.getOrDefault(term, 0) + 1);
				}
				position++;
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

			int docLength = position; // Total number of tokens in document
			long byteSize = d.getByteSize(); // Size of doc in bytes

			DocWeights docWeights = new DocWeights(termFreqMap, docLength, byteSize);
			docWeightsList.add(docWeights);
		}

		long endTime = System.currentTimeMillis(); // End time to build positional Inverted Index

		System.out.println(
				"Time taken to build positional inverted index: " + ((endTime - startTime) / 1000)
						+ " seconds");

		// Write the document weights to disk
		DocWeightsWriter.writeToDisk(docWeightsList);
	}
}
