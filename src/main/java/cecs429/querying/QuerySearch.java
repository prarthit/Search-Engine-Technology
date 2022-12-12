package cecs429.querying;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;

public abstract class QuerySearch {
    public abstract List<Result> findQuery(String query, Index index, DocumentCorpus corpus);

    public void displaySearchResults(List<Result> searchResults, DocumentCorpus corpus, Scanner sc) {
        for (Result result : searchResults) {
            System.out.println(result);
        }

        int queryFoundInFilesCount = searchResults.size();
        System.out.println("Query found in files: " + queryFoundInFilesCount);
        if (queryFoundInFilesCount > 0) {
            // Ask the user if they would like to select a document to view
            System.out.print("Select a document to view (y/n): ");
            String ch = sc.nextLine().toLowerCase();
            if (ch.equals("y")) {
                System.out.print("Enter document name: ");
                String fileName = sc.nextLine();
                try {
                    utils.Utils.readFile(corpus.getCorpusPath() + "/" + fileName);
                } catch (IOException e) {
                    System.out.println("Unable to read the document");
                }
            }
        }
    }

    public void findAndDisplayResults(String query, Index index, DocumentCorpus corpus, Scanner sc) {
        List<Result> results = findQuery(query, index, corpus);
        displaySearchResults(results, corpus, sc);
    }
}
