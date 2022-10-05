package cecs429.querying;

import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.TokenProcessor;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	private TokenProcessor mTokenProcessor;

	public TermLiteral(String term, TokenProcessor tokenProcessor) {
		mTerm = term;
		mTokenProcessor = tokenProcessor;
	}

	public String getTerm() {
		return mTerm;
	}

	@Override
	public List<Posting> getPostings(Index index) {
		String processedQuery = mTokenProcessor.processQuery(mTerm);
		return index.getPostings(processedQuery);
	}

	@Override
	public String toString() {
		return mTerm;
	}
}
