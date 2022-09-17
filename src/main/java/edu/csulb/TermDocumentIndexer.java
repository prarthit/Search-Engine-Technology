package edu.csulb;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.queries.BooleanQueryParser;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class TermDocumentIndexer {
	public static void main(String[] args) throws IOException {
		// Create a DocumentCorpus to load .txt documents from the project directory.
		DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get("all-nps-sites-extracted").toAbsolutePath(),
				".json");
		// Index the documents of the corpus.
		Index index = indexCorpus(corpus);

		// Get the query from user input
		String query = "";

		// When user inputs this string, quit the program
		final String QUIT_STRING = "quit";

		Scanner sc = new Scanner(System.in);

		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

		while (!query.toLowerCase().equals(QUIT_STRING)) {
			System.out.print("Enter a term to search: ");
			query = sc.nextLine();
			String processedQuery = processor.processQuery(query);

			int queryFoundInFilesCount = 0;
			BooleanQueryParser b = new BooleanQueryParser();
			b.parseQuery(query);
			for (Posting p : index.getPostings(processedQuery)) {
				queryFoundInFilesCount++;
				System.out.println("Document: " + corpus.getDocument(p.getDocumentId()).getTitle());
			}
			System.out.println("Query found in files: " + queryFoundInFilesCount);
		}
		sc.close();
	}

	private static Index indexCorpus(DocumentCorpus corpus) throws IOException {
		PositionalInvertedIndex positionalInvertedIndex = new PositionalInvertedIndex();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

		// Add terms to the inverted index with addPosting.
		for (Document d : corpus.getDocuments()) {
			System.out.println("Found document " + d.getTitle());
			Reader content = d.getContent();

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
					positionalInvertedIndex.addTerm(term, documentId, position);
				}
				position++;
			}

			content.close();
			englishTokenStream.close();
		}

		return positionalInvertedIndex;
	}
}
