package edu.csulb;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.FileDocument;
import cecs429.indexing.Biword;
import cecs429.indexing.BiwordInvertedIndex;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.PhraseLiteral;
import cecs429.querying.QueryComponent;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TermDocumentIndexer {
	public static String directoryName = ""; // Directory name entered by the user.
	public static boolean isValidDirectory = false;
	private static boolean isIndexSpecialQueryCalled = false;

	public static void main(String[] args) throws IOException {
		String fileExtension = ".json";
		Scanner sc = new Scanner(System.in);

		do {
			if (!isValidDirectory) {
				// Input Directory from user
				System.out.print("Enter a directory name: ");

				directoryName = sc.nextLine().trim();
				// Print all files and folders in the Input Directory from user
				isValidDirectory = traverseFiles(new File(directoryName));
				continue;
			}
			// Create a DocumentCorpus to load .json documents from the user input
			// directory.
			DocumentCorpus corpus = DirectoryCorpus
					.loadJsonDirectory(Paths.get(new File(directoryName).getAbsolutePath()), fileExtension);
			// Index the documents of the directory using positional inverted index.
			Index index = indexCorpus(corpus);

			// Index the documents of the directory using Biword.
			Index biwordIndex = biwordIndexCorpus(corpus);

			// Get the query from user input
			String query = "";

			AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

			// When user inputs this string, quit the program
			while (!query.toLowerCase().startsWith(":q")) {
				System.out.print("\nEnter a term to search: ");
				query = sc.nextLine();
				
				boolean hasSpecialQueryCalled = processSpecialQueries(query, processor, index);
				if (isIndexSpecialQueryCalled) {
					isIndexSpecialQueryCalled = false;
					break;
				}
				if (!hasSpecialQueryCalled) {
					findQuery(query, index, biwordIndex, corpus, sc);
				}
			}
		} while (!directoryName.isEmpty());
		sc.close();
	}

	private static void findQuery(String query, Index index, Index biwordIndex, DocumentCorpus corpus, Scanner sc) {
		int queryFoundInFilesCount = 0;

		BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
		QueryComponent queryComponent = booleanQueryParser.parseQuery(query);

		if (queryComponent != null) {
			for (Posting p : queryComponent.getPostings(index)) {
				queryFoundInFilesCount++;
				System.out.println("Document: " + corpus.getDocument(p.getDocumentId()).getTitle()
						+ " (FileName: "
						+ ((FileDocument) corpus.getDocument(p.getDocumentId())).getFilePath().getFileName()
								.toString()
						+ " ID: " +
						corpus.getDocument(p.getDocumentId()).getId() + ")");
			}

			System.out.println("Query found in files: " + queryFoundInFilesCount);
			if (queryFoundInFilesCount > 0) {
				// Ask the user if they would like to select a document to view
				System.out.print("Select a document to view (y/n):");
				char ch = sc.nextLine().charAt(0);
				if (ch == 'y' || ch == 'Y') {
					System.out.print("Enter document name:");
					String fileName = sc.nextLine();
					readFile(fileName, directoryName);
				}
			}
		}
	}

	private static boolean processSpecialQueries(String query, AdvancedTokenProcessor processor, Index index) {
		if (query.toLowerCase().startsWith(":q")) {
			directoryName = "";
		} else if (query.toLowerCase().startsWith(":stem")) {
			query = query.replaceAll(":stem", "").trim();

			List<String> stemmedTerms = processor.processToken(query);
			stemmedTerms.forEach(stemmedTerm -> System.out.println(stemmedTerm));
		} else if (query.toLowerCase().startsWith(":index")) {
			query = query.replaceAll(":index", "").trim();

			isValidDirectory = traverseFiles(new File(query));
			if (isValidDirectory) {
				directoryName = query;
				isIndexSpecialQueryCalled = true;
			} else {
				System.out.println("Please try searching another query.");
			}
		} else if (query.toLowerCase().startsWith(":vocab")) {
			query = query.replaceAll(":vocab", "").trim();

			List<String> topThousandTerms = index.getVocabulary();
			int sizeVocabularyterms = topThousandTerms.size();
			for (int i = 0; i < sizeVocabularyterms && i != 1000; i++) {
				System.out.println(topThousandTerms.get(i));
			}
			System.out.println(sizeVocabularyterms);
		} else {
			return false;
		}

		return true;
	}

	private static Index indexCorpus(DocumentCorpus corpus) throws IOException {
		long startTime; // Start time to build positional Inverted Index
		long endTime; // End time to build positional Inverted Index

		PositionalInvertedIndex positionalInvertedIndex = new PositionalInvertedIndex();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

		// Start time to build positional Iverted Index
		startTime = System.nanoTime();

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

		// end time to build inverted positional index
		endTime = System.nanoTime();
		// total time taken to build inverted positional index
		System.out.println(
				"Time taken to build inverted positional index: " + ((endTime - startTime) / 1000000000) + " seconds");

		return positionalInvertedIndex;
	}

	private static Index biwordIndexCorpus(DocumentCorpus corpus) throws IOException {
		long startTime; // Start time to build positional Inverted Index
		long endTime; // End time to build positional Inverted Index

		BiwordInvertedIndex biwordInvertedIndex = new BiwordInvertedIndex();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

		// Start time to build biword Iverted Index
		startTime = System.nanoTime();

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

			// Build biword inverted index
			buildBiwordIndex(d, tokens, processor, biwordInvertedIndex);

			content.close();
			englishTokenStream.close();
		}

		// end time to build inverted biword index
		endTime = System.nanoTime();
		// total time taken to build inverted positional index
		System.out.println(
				"Time taken to build inverted biword index: " + ((endTime - startTime) / 1000000000) + " seconds");

		return biwordInvertedIndex;
	}

	private static void buildBiwordIndex(Document d, Iterator<String> tokens, AdvancedTokenProcessor processor, BiwordInvertedIndex biwordInvertedIndex) {
		String prevTerm = null;
		while (tokens.hasNext()) {
			List<String> terms = processor.processToken(tokens.next());
			int documentId = d.getId();
			if(terms.size() == 1){
				if(prevTerm != null){
					biwordInvertedIndex.addTerm(new Biword(prevTerm, terms.get(0)), documentId); 
				}
				prevTerm = terms.get(0);;
			}else{
				// generate biwords when tokens are of form "co-education"
				for (Biword biword : generateBiwords(terms, prevTerm)) {
					biwordInvertedIndex.addTerm(biword, documentId);
				}
			}
		}
	}

	private static List<Biword> generateBiwords(List<String> terms, String prevTerm) {
        List<Biword> biwordResult = new ArrayList<Biword>();
        for (String term : terms) {
            if (prevTerm != null){
                Biword biword = new Biword(prevTerm, term);
                biwordResult.add(biword);
            }
			prevTerm = term;
        }
        return biwordResult;
    }

	private static boolean traverseFiles(File inputFile) {
		File[] listFiles = inputFile.listFiles();
		if (listFiles == null) {
			return false;
		}
		for (File file : listFiles) {
			if (file.isDirectory()) {
				System.out.println("Directory:" + file.getAbsolutePath());
				traverseFiles(file);
			} else {
				System.out.println("\tFile:" + file.getAbsolutePath());
			}
		}

		return true;
	}

	public static void readFile(String fileName, String fileDirectory) {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(fileDirectory + "/" + fileName));
			JSONObject jsonObject = (JSONObject) obj;
			System.out.println(jsonObject.get("body"));
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			System.out.println("Please try searching again.");
		}
	}
}
