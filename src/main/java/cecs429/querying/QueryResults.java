package cecs429.querying;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.FileDocument;
import cecs429.indexing.Posting;

public abstract class QueryResults {
    public void displaySearchResults(List<Posting> searchResultPostings, List<Double> accumulatorValues,
            DocumentCorpus corpus, Scanner sc) {
        int queryFoundInFilesCount = 0, i = 0;

        for (Posting p : searchResultPostings) {
            Document queryFoundInDocument = corpus.getDocument(p.getDocumentId());
            System.out.print(queryFoundInDocument.getTitle()
                    + " (FileName: "
                    + ((FileDocument) queryFoundInDocument).getFilePath().getFileName().toString()
                    + ")");
            if (accumulatorValues != null) {
                System.out.print("(Accumulator: " + accumulatorValues.get(i) + ")");
            }
            System.out.println();

            queryFoundInFilesCount++;
            i++;
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
                    utils.Utils.readFile(corpus.getCorpusPath() + "/" + fileName);
                } catch (IOException e) {
                    System.out.println("Unable to read the document");
                }
            }
        }
    }
}
