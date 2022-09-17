package cecs429.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = index.getPostings(mTerms.get(0).toString());
		for(int i=1;i<mTerms.size()-1;i++){
			List<Posting> literalPostings = index.getPostings(mTerms.get(i+1).toString());
			result = positionalIntersect(result, literalPostings, i);
		}
		
		return result;
		// TODO: program this method. Retrieve the postings for the individual terms in the phrase,
		// and positional merge them together.
		// parseQuery can then be executed by calling its
		// getPostings method, passing the PositionalInvertedIndex you constructed earlier in the program.
	}
	
	private int documentID(Posting posting){
		return posting.getDocumentId();
	}

	private List<Integer> positions(Posting posting){
		return posting.getPositions();
	}
	
	private List<Posting> positionalIntersect(List<Posting> literalPostings1, List<Posting> literalPostings2, int k) {
		List<Posting> res = new ArrayList<>();                                                                   
        int len1 = literalPostings1.size();
        int len2 = literalPostings2.size();
        int i = 0;
		int j = 0; 
        while(i != len1 && j != len2){
			Posting p1 = literalPostings1.get(i);
			Posting p2 = literalPostings2.get(j);

			if(documentID(p1) == documentID(p2)){
				List<Integer> l = new ArrayList<>();  
				List<Integer> postingPosition1 = positions(p1);
				List<Integer> postingPosition2 = positions(p2);

				int plen1 = postingPosition1.size();
				int plen2 = postingPosition2.size();

				int m = 0;
				int n = 0;

				while(m!=plen1){
					while(n!=plen2){
						if(Math.abs(postingPosition1.get(m) - postingPosition2.get(n)) <= k){
							l.add(postingPosition2.get(n));
						}
						else if(postingPosition2.get(n) > postingPosition1.get(m)){
							break;
						}
						n++;
					}

					while(!l.isEmpty() && Math.abs(l.get(0)-postingPosition1.get(m)) > k){
						l.remove(l.get(0));
					}
					m++;
				}
				res.add(new Posting(documentID(p1), l));

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
		return res;
	}


	@Override
	public String toString() {
		String terms = 
			mTerms.stream()
			.collect(Collectors.joining(" "));
		return "\"" + terms + "\"";
	}
}
