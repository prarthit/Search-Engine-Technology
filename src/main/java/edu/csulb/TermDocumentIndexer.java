package edu.csulb;

import java.io.File;
import java.io.FileReader;
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
import cecs429.queries.QueryComponent;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TermDocumentIndexer {
	public static void main(String[] args) throws IOException {
		String directoryName; // Directory name entered by the user.
		String fileExtension = ".json";
		// Input Directory from user
		System.out.print("Enter a directory name: ");
		Scanner sc = new Scanner(System.in);
		directoryName = sc.nextLine().trim();
		boolean isDirectoryPresent = false;

		// Print all files and folders in the Input Directory from user
		isDirectoryPresent = traverseFiles(new File(directoryName));
		do {
			while (!isDirectoryPresent) {
				System.out.println("Please try again with correct directory name.");
				System.out.print("Do you want to continue (Y/N):");
				String ch = sc.nextLine();
				if (ch.toLowerCase().equals("n")) {
					System.exit(0);
				}

				System.out.print("Enter a directory name: ");
				directoryName = sc.nextLine().trim();
				isDirectoryPresent = traverseFiles(new File(directoryName));
			}
			System.out.println("Listing the files/folders of input directory - " + directoryName);
			// Create a DocumentCorpus to load .json documents from the user input
			// directory.
			DocumentCorpus corpus = DirectoryCorpus
					.loadJsonDirectory(Paths.get(new File(directoryName).getAbsolutePath()), fileExtension);
			// Index the documents of the directory.
			Index index = indexCorpus(corpus);
			// Get the query from user input
			String query = "";
			// When user inputs this string, quit the program

			AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

			while (!query.toLowerCase().startsWith(":q")) {
				System.out.print("Enter a term to search: ");
				query = sc.nextLine();

				if (query.toLowerCase().startsWith(":q")) {
					directoryName = "";
				} else if (query.toLowerCase().startsWith(":stem")) {
					query = query.replaceAll(":stem", "").trim();

					List<String> stemmedTerms = processor.processToken(query);
					stemmedTerms.forEach(stemmedTerm -> System.out.println(stemmedTerm));
				} else if (query.toLowerCase().startsWith(":index")) {
					query = query.replaceAll(":index", "").trim();

					isDirectoryPresent = traverseFiles(new File(query));
					if (isDirectoryPresent) {
						directoryName = query;
						break;
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
					int queryFoundInFilesCount = 0;

					BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
					QueryComponent queryComponent = booleanQueryParser.parseQuery(query);

					for (Posting p : queryComponent.getPostings(index)) {
						queryFoundInFilesCount++;
						System.out.println("Document: " + corpus.getDocument(p.getDocumentId()).getTitle()
								+ " {FileName: " + corpus.getDocument(p.getDocumentId()).getId() + fileExtension + "}");
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
		} while (!directoryName.isEmpty());
		sc.close();
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
