package cecs429.querying;

import java.util.List;
import java.util.Scanner;

import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.Posting;

public class BooleanQuerySearch extends QueryResults {
    public void findQuery(QueryComponent queryComponent, Index index, DocumentCorpus corpus, Scanner sc) {

        if (queryComponent != null) {
            List<Posting> searchResultPostings = queryComponent.getPostings(index);
            displaySearchResults(searchResultPostings, corpus, sc);
        }
    }
}
