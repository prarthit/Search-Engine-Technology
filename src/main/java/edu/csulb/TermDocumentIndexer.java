package edu.csulb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.FileDocument;
import cecs429.indexing.Index;
import cecs429.indexing.KGramIndex;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.QueryComponent;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class TermDocumentIndexer {
	private static String prevDirectoryPath = "", newDirectoryPath = ""; // Directory name where the corpus resides

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);

		do {
			System.out.print("Enter directory path: ");
			newDirectoryPath = sc.nextLine().trim();
		} while (!isValidDirectory(newDirectoryPath));

		DocumentCorpus corpus = null;
		Index index = null;
		BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

		// Loop for taking search input query
		while (true) {
			// If a newDirectory is provided, build a new index
			if (!prevDirectoryPath.equals(newDirectoryPath)) {
				prevDirectoryPath = newDirectoryPath;

				String fileExtension = ".json";
				// Create a DocumentCorpus to load .json documents from the user input
				// directory.
				corpus = DirectoryCorpus
						.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()), fileExtension);
				// Index the documents of the directory.
				index = indexCorpus(corpus);

				// Build a k-gram index from the corpus
				KGramIndex kGramIndex = buildKGramIndex(corpus);
				booleanQueryParser.setKGramIndex(kGramIndex);
			}

			System.out.print("\nEnter a search query: ");
			// Get the query from user input
			String query = sc.nextLine().toLowerCase();

			boolean isSpecialQuery = processSpecialQueries(query, processor, index);
			if (isSpecialQuery) {
				if (query.equals(":q"))
					break;
			} else {
				QueryComponent queryComponent = booleanQueryParser.parseQuery(query);
				findQuery(queryComponent, index, corpus, sc);
			}
		}

		return;
	}

	public static boolean isValidDirectory(String directoryPath) {
		boolean isValidDirectory = Files.isDirectory(Paths.get(directoryPath));
		if (!isValidDirectory)
			System.out.println("Invalid directory path");
		return isValidDirectory;
	}

	private static void findQuery(QueryComponent queryComponent, Index index, DocumentCorpus corpus, Scanner sc) {
		int queryFoundInFilesCount = 0;
		List <List<Integer>> DocumentList = new ArrayList<>();
		List <Integer> positionsList = new ArrayList<>(); 

		if (queryComponent != null) {
			for (Posting p : queryComponent.getPostings(index)) {
				queryFoundInFilesCount++;
				Document queryFoundInDocument = corpus.getDocument(p.getDocumentId());
				positionsList.add(p.getDocumentId() + 1);
				System.out.println(queryFoundInDocument.getTitle()
						+ " (FileName: "
						+ ((FileDocument) queryFoundInDocument).getFilePath().getFileName().toString()
						+ ")");
				p.getPositions().forEach(Position -> positionsList.add(Position + 1));
			}
			DocumentList.add(positionsList);
			System.out.println(DocumentList);

			System.out.println(" Query found in files: " + queryFoundInFilesCount);
			if (queryFoundInFilesCount > 0) {
				// Ask the user if they would like to select a document to view
				System.out.print("Select a document to view (y/n): ");
				String ch = sc.nextLine().toLowerCase();
				if (ch.equals("y")) {
					System.out.print("Enter document name: ");
					String fileName = sc.nextLine();
					try {
						readFile(newDirectoryPath + "/" + fileName);
					} catch (IOException e) {
						System.out.println("Unable to read file");
					}
				}
			}
		}
	}

	public static boolean processSpecialQueries(String query, AdvancedTokenProcessor processor, Index index) {
		if (query.equals(":q")) {
		} else if (query.startsWith(":stem ")) {
			query = query.replaceAll(":stem ", "").trim();
			List<String> stemmedTerms = processor.processToken(query);
			stemmedTerms.forEach(stemmedTerm -> System.out.println(stemmedTerm));
		} else if (query.startsWith(":index ")) {
			query = query.replaceAll(":index ", "").trim();

			if (isValidDirectory(query)) {
				newDirectoryPath = query;
			}
		} else if (query.equals(":vocab")) {
			List<String> vocabulary = index.getVocabulary();
			int termsCount = Math.min(vocabulary.size(), 1000);
			for (int i = 0; i < termsCount; i++) {
				System.out.println(vocabulary.get(i));
			}
			System.out.println(vocabulary.size());
		} else {
			// The query is not a special query
			return false;
		}

		return true;
	}

	public static Index indexCorpus(DocumentCorpus corpus) throws IOException {
		long startTime = System.currentTimeMillis(); // Start time to build positional Inverted Index
		System.out.println("Indexing...");
		PositionalInvertedIndex positionalInvertedIndex = new PositionalInvertedIndex();
		TokenProcessor processor = new AdvancedTokenProcessor();

		// Add terms to the inverted index with addPosting.
		for (Document d : corpus.getDocuments()) {
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

		long endTime = System.currentTimeMillis(); // End time to build positional Inverted Index

		System.out.println(
				"Time taken to build positional inverted index: " + ((endTime - startTime) / 1000)
						+ " seconds");

		return positionalInvertedIndex;
	}

	private static KGramIndex buildKGramIndex(DocumentCorpus corpus) throws IOException {
		long startTime = System.currentTimeMillis(); // Start time to build k-gram index
		System.out.println("Building k-gram index...");

		HashSet<String> vocabulary = new HashSet<>();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

		// Add terms to the inverted index with addPosting.
		for (Document d : corpus.getDocuments()) {
			Reader content = d.getContent();

			// Tokenize the document's content by constructing an EnglishTokenStream around
			// the document's content.
			EnglishTokenStream englishTokenStream = new EnglishTokenStream(content);

			// Iterate through the tokens in the document, processing them
			// using a BasicTokenProcessor, and adding them to the
			// positional inverted index dictionary.
			Iterator<String> tokens = englishTokenStream.getTokens().iterator();
			while (tokens.hasNext()) {
				String term = processor.preProcessToken(tokens.next());
				vocabulary.add(term);
			}

			content.close();
			englishTokenStream.close();
		}

		// Sort the vocabulary
		List<String> vocabulary_list = new ArrayList<String>(vocabulary);
		Collections.sort(vocabulary_list);

		KGramIndex kGramIndex = new KGramIndex(vocabulary_list);

		long endTime = System.currentTimeMillis(); // End time to build k-gram index

		System.out.println(kGramIndex.getKGrams().size() + " distinct kgrams in index");
		System.out.println(
				"Time taken to build k-gram index: " + ((endTime - startTime) / 1000)
						+ " seconds");

		return kGramIndex;
	}

	// Generic file reader
	public static BufferedReader readFile(String filepath) throws IOException {
		BufferedReader in;
		in = new BufferedReader(new FileReader(filepath));
		String line = in.readLine();
		while (line != null) {
			System.out.println(line);
			line = in.readLine();
		}
		return in;
	}
}
