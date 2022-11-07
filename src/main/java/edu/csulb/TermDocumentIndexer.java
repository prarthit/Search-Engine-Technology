package edu.csulb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.FileDocument;
import cecs429.indexing.BiwordInvertedIndex;
import cecs429.indexing.Index;
import cecs429.indexing.KGramIndex;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.indexing.diskIndex.DiskIndexEnum;
import cecs429.indexing.diskIndex.DiskIndexWriter;
import cecs429.indexing.diskIndex.DiskPositionalIndex;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.QueryComponent;
import cecs429.text.TokenProcessor;
import utils.Utils;

public class TermDocumentIndexer {
	private static String prevDirectoryPath = "", newDirectoryPath = ""; // Directory name where the corpus resides

	public static void main(String[] args) throws IOException, SQLException {
		Properties prop = new Properties();
		prop.load(new FileInputStream("src/config.properties"));

		System.out.println("Reading corpus directory path from config.properties file");
		newDirectoryPath = prop.getProperty("corpus_directory_path");
		Scanner sc = new Scanner(System.in);
		while (!Utils.isValidDirectory(newDirectoryPath)) {
			System.out.print("Enter directory path: ");
			newDirectoryPath = sc.nextLine().trim();
		}

		DocumentCorpus corpus = null;
		Index index = null;
		BooleanQueryParser booleanQueryParser = new BooleanQueryParser();

		// Create basic or advanced token processor based on properties file
		TokenProcessor processor = Utils.getTokenProcessor(prop.getProperty("token_processor"));
		booleanQueryParser.setTokenProcessor(processor);

		// Loop for taking search input query
		while (true) {
			// If a newDirectory is provided, build a new index
			if (!prevDirectoryPath.equals(newDirectoryPath)) {
				prevDirectoryPath = newDirectoryPath;

				// Create a DocumentCorpus to load either .txt or .json documents from the user
				// input directory.
				corpus = DirectoryCorpus
						.loadDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()));

				String postingsFileName = DiskIndexEnum.POSITIONAL_INDEX.getPostingFileName();
				File binsDirectory = Utils.createDirectory(prop.getProperty("resources_dir"));
				String childDirectoryName = Utils.getChildDirectoryName(newDirectoryPath);

				String diskDirectoryPath = binsDirectory.getAbsolutePath() + "/" + childDirectoryName;
				File diskFilePath = new File(diskDirectoryPath + postingsFileName);

				if (diskFilePath.exists() && diskFilePath.length() > 0) {
					// Read from the already existed disk index
					index = new DiskPositionalIndex(diskDirectoryPath);
				} else {
					// Index the documents of the directory.
					index = new PositionalInvertedIndex(corpus, processor);
					// Build and write the disk index
					DiskIndexWriter dWriter = new DiskIndexWriter(index, diskDirectoryPath);
					dWriter.writeIndex();
				}

				// Build a k-gram index from the corpus
				KGramIndex kGramIndex = new KGramIndex(corpus);
				booleanQueryParser.setKGramIndex(kGramIndex);

				// Build a biword index from the corpus
				Index biwordIndex = new BiwordInvertedIndex(corpus, processor);
				booleanQueryParser.setBiwordIndex(biwordIndex);
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

		sc.close();

		return;
	}

	private static void findQuery(QueryComponent queryComponent, Index index, DocumentCorpus corpus, Scanner sc) {
		int queryFoundInFilesCount = 0;

		if (queryComponent != null) {
			for (Posting p : queryComponent.getPostings(index)) {
				queryFoundInFilesCount++;
				Document queryFoundInDocument = corpus.getDocument(p.getDocumentId());
				System.out.println(queryFoundInDocument.getTitle()
						+ " (FileName: "
						+ ((FileDocument) queryFoundInDocument).getFilePath().getFileName().toString()
						+ ")");
			}

			System.out.println("Query found in files: " + queryFoundInFilesCount);
			if (queryFoundInFilesCount > 0) {
				// Ask the user if they would like to select a document to view
				System.out.print("Select a document to view (y/n): ");
				String ch = sc.nextLine().toLowerCase();
				if (ch.equals("y")) {
					System.out.print("Enter document name: ");
					String fileName = sc.nextLine();
					try {
						utils.Utils.readFile(newDirectoryPath + "/" + fileName);
					} catch (IOException e) {
						System.out.println("Unable to read the document");
					}
				}
			}
		}
	}

	public static boolean processSpecialQueries(String query, TokenProcessor processor, Index index) {
		if (query.equals(":q")) {
		} else if (query.startsWith(":stem ")) {
			query = query.replaceAll(":stem ", "").trim();
			List<String> stemmedTerms = processor.processToken(query);
			stemmedTerms.forEach(stemmedTerm -> System.out.println(stemmedTerm));
		} else if (query.startsWith(":index ")) {
			query = query.replaceAll(":index ", "").trim();

			if (Utils.isValidDirectory(query)) {
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
}
