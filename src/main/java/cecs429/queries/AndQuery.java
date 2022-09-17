package cecs429.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.AdvancedTokenProcessor;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;
	
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {		
		//Process token before calling getPosting
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
		String processedQuery = processor.processQuery(mComponents.get(0).toString());
		List<Posting> result = index.getPostings(processedQuery);

		for(int i=1;i<mComponents.size();i++){
			List<Posting> postingList1 = index.getPostings(processor.processQuery(mComponents.get(i).toString()));
			result = intersectPostingDocumentIds(result, postingList1);
		}

		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		return result;
	}

	private int documentID(Posting posting){
		return posting.getDocumentId();
	}

	private List<Posting> intersectPostingDocumentIds(List<Posting> literalPostings1, List<Posting> literalPostings2) {
		List<Posting> result = new ArrayList<Posting>();                                                                   
        int len1 = literalPostings1.size();
        int len2 = literalPostings2.size();
        int i = 0;
		int j = 0; 
        while(i != len1 && j != len2){
			Posting p1 = literalPostings1.get(i);
			Posting p2 = literalPostings2.get(j);

			if(documentID(p1) == documentID(p2)){
				result.add(new Posting(documentID(p1)));
				i++;
				j++;
			}
			else if(documentID(p1) < documentID(p2)){
				i++;
			}
			else{
				j++;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
