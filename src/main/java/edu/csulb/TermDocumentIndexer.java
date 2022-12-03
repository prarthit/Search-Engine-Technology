package edu.csulb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.BiwordInvertedIndex;
import cecs429.indexing.Index;
import cecs429.indexing.KGramIndex;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.diskIndex.DiskBiwordIndex;
import cecs429.indexing.diskIndex.DiskIndexEnum;
import cecs429.indexing.diskIndex.DiskIndexWriter;
import cecs429.indexing.diskIndex.DiskIndexWriterEncoded;
import cecs429.indexing.diskIndex.DiskPositionalIndex;
import cecs429.indexing.diskIndex.DiskPositionalIndexDecoded;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.BooleanQuerySearch;
import cecs429.querying.QuerySearch;
import cecs429.querying.RankedQuerySearch;
import cecs429.text.TokenProcessor;
import utils.Utils;

public class TermDocumentIndexer {
	private static String prevDirectoryPath = "", newDirectoryPath = ""; // Directory name where the corpus resides

	public static void main(String[] args) throws IOException, SQLException {
		Properties prop = Utils.getProperties();

		System.out.println("Reading corpus directory path from config.properties file");
		newDirectoryPath = prop.getProperty("corpus_directory_path");

		Scanner sc = new Scanner(System.in);
		while (!Utils.isValidDirectory(newDirectoryPath)) {
			System.out.println("Invalid directory path");
			System.out.print("Enter directory path: ");
			newDirectoryPath = sc.nextLine().trim();
		}

		DocumentCorpus corpus = null;
		Index index = null;
		Index biwordIndex = null;
		KGramIndex kGramIndex = null;

		// Create basic or advanced token processor based on properties file
		TokenProcessor processor = EngineStore.getTokenProcessor();

		// Query search engine - either boolean or ranked
		QuerySearch querySearchEngine = null;

		// Loop for taking search input query
		while (true) {
			// If a newDirectory is provided, build a new index
			if (!prevDirectoryPath.equals(newDirectoryPath)) {
				prevDirectoryPath = newDirectoryPath;

				// Create a DocumentCorpus to load either .txt or .json documents from the user
				// input directory.
				corpus = DirectoryCorpus
						.loadDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()));
				Utils.setCorpusName(corpus.getCorpusName());

				String diskDirPath = Utils.generateFilePathPrefix();
				String positionalIndexFilePath = diskDirPath + DiskIndexEnum.POSITIONAL_INDEX.getIndexFileName();
				File positionalIndexFile = new File(positionalIndexFilePath);

				if (positionalIndexFile.exists() && positionalIndexFile.length() > 0) {
					// Call the corpus get documents to set the hashmap for corpus mDocuments
					corpus.getDocuments();

					// Read from the already existed disk index
					if (prop.getProperty("variable_byte_encoding").equals("true")) {
						index = new DiskPositionalIndexDecoded(diskDirPath);
					} else {
						index = new DiskPositionalIndex(diskDirPath);
					}
					biwordIndex = new DiskBiwordIndex(diskDirPath);
				} else {
					// Index the documents of the directory.
					index = new PositionalInvertedIndex(corpus, processor);
					biwordIndex = new BiwordInvertedIndex(corpus, processor);

					DiskIndexWriter dWriter = new DiskIndexWriter();
					// Build and write the disk index
					if (prop.getProperty("variable_byte_encoding").equals("true")) {
						DiskIndexWriterEncoded dWriterCompressed = new DiskIndexWriterEncoded(index,
								diskDirPath);
						dWriterCompressed.writeIndex();
					} else {
						dWriter.setPositionalIndex(index, diskDirPath);
						dWriter.writeIndex();
					}

					dWriter.setBiwordIndex(biwordIndex, diskDirPath);
					dWriter.writeBiwordIndex();
				}

				EngineStore.setIndex(index);

				// Build a k-gram index from the corpus
				kGramIndex = new KGramIndex(corpus);
				EngineStore.setkGramIndex(kGramIndex);

				// Build a biword index from the corpus
				EngineStore.setBiwordIndex(biwordIndex);

				String query_mode = prop.getProperty("query_mode");
				if (query_mode.equals("BOOLEAN")) {
					BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
					booleanQueryParser.setKGramIndex(EngineStore.getkGramIndex());
					booleanQueryParser.setBiwordIndex(EngineStore.getBiwordIndex());
					booleanQueryParser.setTokenProcessor(EngineStore.getTokenProcessor());

					querySearchEngine = new BooleanQuerySearch(booleanQueryParser);
				} else {
					int k = Integer.parseInt(prop.getProperty("num_results"));
					String ranking_score_scheme = prop.getProperty("ranking_score_scheme");
					querySearchEngine = new RankedQuerySearch(k, ranking_score_scheme, processor);
				}
			}

			System.out.print("\nEnter a search query: ");
			// Get the query from user input
			String query = sc.nextLine().trim().toLowerCase();

			boolean isSpecialQuery = processSpecialQueries(query, processor, index);
			if (isSpecialQuery) {
				if (query.equals(":q"))
					break;
			} else {
				querySearchEngine.findAndDisplayResults(query, index, corpus, sc);
			}
		}

		sc.close();

		return;
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
			} else {
				System.out.println("Invalid directory path");
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
