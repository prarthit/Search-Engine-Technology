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
import cecs429.documents.FileDocument;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.QueryComponent;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;


public class TermDocumentIndexer {
	public static void main(String[] args) throws IOException {
		String directoryName = ""; // Directory name entered by the user.
		String fileExtension = ".json";
		boolean isValidDirectory = false;
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
			// System.out.println("Listing the files/folders of input directory - " +
			// directoryName);
			// Create a DocumentCorpus to load .json documents from the user input
			// directory.
			DocumentCorpus corpus = DirectoryCorpus
					.loadJsonDirectory(Paths.get(new File(directoryName).getAbsolutePath()), fileExtension);
			// Index the documents of the directory.
			Index index = indexCorpus(corpus);
			// Get the query from user input
			String query = "";

			AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

			// When user inputs this string, quit the program
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

					isValidDirectory = traverseFiles(new File(query));
					if (isValidDirectory) {
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
								readFile(directoryName + "/" + fileName);
							}
						}
					}
				}
			}
		} while (!directoryName.isEmpty());
		sc.close();
	}

	private static Index indexCorpus(DocumentCorpus corpus) throws IOException {
		PositionalInvertedIndex positionalInvertedIndex = new PositionalInvertedIndex();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
		long startTime = System.currentTimeMillis(); // Start time to build positional Inverted Index

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

		long endTime = System.currentTimeMillis(); // End time to build positional Inverted Index

		// total time taken to build inverted positional index
		System.out.println("Time taken to build inverted positional index: " + ((endTime - startTime) / 1000) + " seconds");
		return positionalInvertedIndex;
	}

	// Treverse and check if files are present in the user input directory.
	private static boolean traverseFiles(File inputFile) {
		File[] listFiles = inputFile.listFiles();
		if (listFiles == null) {
			return false;
		}
		return true;
	}

	// Generic file reader 
	public static void readFile(String filepath) {
		try {
			FileReader fr = new FileReader(filepath);
			int i;
			while ((i = fr.read()) != -1){
				System.out.print((char)i);
			}
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
