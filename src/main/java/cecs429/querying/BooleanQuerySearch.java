package cecs429.querying;

import java.util.ArrayList;
import java.util.List;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.Posting;

public class BooleanQuerySearch extends QuerySearch {
    private BooleanQueryParser booleanQueryParser;

    public BooleanQuerySearch(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

    public List<Result> findQuery(String query, Index index, DocumentCorpus corpus) {
        QueryComponent queryComponent = booleanQueryParser.parseQuery(query);
        if (queryComponent != null) {
            List<Posting> searchResultsPostings = queryComponent.getPostings(index);
            List<Result> searchResults = new ArrayList<>();
            for (Posting p : searchResultsPostings) {
                Document document = corpus.getDocument(p.getDocumentId());
                searchResults.add(new Result(document));
            }
            return searchResults;
        }

        return new ArrayList<>();
    }
}
