package cecs429.queries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.AdvancedTokenProcessor;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		//Process token before calling getPosting
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
		String processedQuery = processor.processQuery(mComponents.get(0).toString());
		List<Posting> result = index.getPostings(processedQuery);

		for(int i=1;i<mComponents.size();i++){
			// change ProcessToken for QueryComponent
			List<Posting> postingList1 = index.getPostings(processor.processQuery(mComponents.get(i).toString()));
			result = intersectPostingDocumentIds(result, postingList1);
		}

		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		return result;
	}

	private List<Posting> intersectPostingDocumentIds(List<Posting> literalPostings1, List<Posting> literalPostings2) {
		literalPostings1.addAll(literalPostings2);

		HashSet<Integer> seen = new HashSet<>();
		literalPostings1.removeIf(e->!seen.add(e.getDocumentId()));	

		return literalPostings1; 
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
