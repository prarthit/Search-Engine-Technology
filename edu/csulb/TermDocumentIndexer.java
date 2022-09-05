package edu.csulb;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.indexing.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class TermDocumentIndexer {
	public static void main(String[] args) {
		// Create a DocumentCorpus to load .txt documents from the project directory.
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
		// Index the documents of the corpus.
		Index index = indexCorpus(corpus) ;

		// Get the query from user input
		String query = "";
		// When user inputs this string, quit the program
		final String QUIT_STRING = "quit";
		Scanner sc = new Scanner(System.in);
		while(!query.toLowerCase().equals(QUIT_STRING)){
			System.out.print("Enter a term to search: ");
			query = sc.nextLine();
			for(Posting p: index.getPostings(query)){
				System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
			}
		};
		sc.close();
	}
	
	private static Index indexCorpus(DocumentCorpus corpus) {
		HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		
		// First, build the vocabulary hash set.
		for (Document d : corpus.getDocuments()) {
			System.out.println("Found document " + d.getTitle());
			// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
			EnglishTokenStream englishTokenStream = new EnglishTokenStream(d.getContent());
			
			// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
			// and adding them to the HashSet vocabulary.
			Iterator<String> tokens = englishTokenStream.getTokens().iterator();
			while(tokens.hasNext()){
				String term = processor.processToken(tokens.next());
				vocabulary.add(term);
			}
		}
		
		// Constuct a TermDocumentMatrix once you know the size of the vocabulary.
		int corpusSize = corpus.getCorpusSize();
		TermDocumentIndex termDocumentIndex = new TermDocumentIndex(vocabulary, corpusSize);

		// THEN, do the loop again! But instead of inserting into the HashSet, add terms to the index with addPosting.
		for(Document d: corpus.getDocuments()){
			EnglishTokenStream englishTokenStream = new EnglishTokenStream(d.getContent());
			Iterator<String> tokens = englishTokenStream.getTokens().iterator();
			while(tokens.hasNext()){
				String term = processor.processToken(tokens.next());
				int documentId = d.getId();
				termDocumentIndex.addTerm(term, documentId);
			}
		}
		
		return termDocumentIndex;
	}
}
