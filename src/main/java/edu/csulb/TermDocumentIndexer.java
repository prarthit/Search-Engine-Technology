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
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.KGramIndex;
import cecs429.indexing.diskIndex.DiskBiwordIndex;
import cecs429.indexing.diskIndex.DiskIndexWriter;
import cecs429.indexing.diskIndex.DiskPositionalIndex;
import cecs429.indexing.diskIndex.DiskPositionalIndexDecoded;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.BooleanQuerySearch;
import cecs429.querying.QueryComponent;
import cecs429.querying.RankedQuerySearch;
import cecs429.querying.WildcardLiteral;
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
		Index biwordIndex = null;
		KGramIndex kGramIndex = null;
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
				Utils.setCorpusName(corpus.getCorpusName());

				String diskDirPath = Utils.generateFilePathPrefix();
				corpus.getDocuments();

				DiskIndexWriter dWriter = new DiskIndexWriter();

				dWriter.setMetrics(diskDirPath, processor, corpus);
				
				// Set the batch limit for the insert operation
				dWriter.setMaximumBatchLimit(Integer.parseInt(prop.getProperty("maximum_batch_size")));

				// Write positional Index in disk
				dWriter.writeIndex();
				// Write positional encoded Index in disk
				dWriter.writeIndexEncoded();
				// Write positional Impact ordering Index in disk
				dWriter.writeImpactOrderingIndex();
				// Write biword Index in disk
				dWriter.writeBiwordIndex();

				// Build a k-gram index from the corpus
				kGramIndex = new KGramIndex(corpus);
				booleanQueryParser.setKGramIndex(kGramIndex);

				// Read from the already existed disk index
				if (prop.getProperty("variable_byte_encoding").equals("true")) {
					index = new DiskPositionalIndexDecoded(diskDirPath);
				} else {
					index = new DiskPositionalIndex(diskDirPath);
				}

				// Build a biword index from the corpus
				biwordIndex = new DiskBiwordIndex(diskDirPath);
				booleanQueryParser.setBiwordIndex(biwordIndex);
			}

			System.out.print("\nEnter a search query: ");
			// Get the query from user input
			String query = sc.nextLine().trim().toLowerCase();

			boolean isSpecialQuery = processSpecialQueries(query, processor, index);
			if (isSpecialQuery) {
				if (query.equals(":q"))
					break;
			} else {
				String query_mode = prop.getProperty("query_mode");
				if (query_mode.equals("BOOLEAN")) {
					QueryComponent queryComponent = booleanQueryParser.parseQuery(query);
					(new BooleanQuerySearch()).findQuery(queryComponent, index, corpus, sc);
				} else {
					int k = Integer.parseInt(prop.getProperty("num_results"));
					String ranking_score_scheme = prop.getProperty("ranking_score_scheme");
					WildcardLiteral wildcardLiteral = new WildcardLiteral("", processor, kGramIndex);
					(new RankedQuerySearch(k, ranking_score_scheme, wildcardLiteral)).findQuery(query, index, corpus,
							sc, processor);
				}
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
