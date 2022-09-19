package cecs429.queries;

import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.AdvancedTokenProcessor;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	
	public TermLiteral(String term) {
		mTerm = term;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
		String processedQuery = processor.processQuery(mTerm);
		return index.getPostings(processedQuery);
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
