package cecs429.querying;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;

	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<>();
		boolean impact_ordering = false;
		if (mComponents.size() == 0) {
			return result;
		}

		result = mComponents.get(0).getPostings(index);

		if(!utils.Utils.isSortedList(result)){
			impact_ordering = true;
		}

		for (int i = 1; i < mComponents.size(); i++) {
			List<Posting> postingList1 = mComponents.get(i).getPostings(index);

			if(impact_ordering || !utils.Utils.isSortedList(postingList1)){
				result = intersectPostingUsingHashSet(result, postingList1);
			}
			else
				result = intersectPostingDocumentIds(result, postingList1);
		}

		return result;
	}

	private List<Posting> intersectPostingUsingHashSet(List<Posting> literalPostings1, List<Posting> literalPostings2) {
		List<Posting> result = new ArrayList<Posting>();
		int len2 = literalPostings2.size();

		HashSet<Integer> hashsetPosting = new HashSet<>();
		for(Posting p : literalPostings1){
			hashsetPosting.add(p.getDocumentId());
		}

		int i = 0;
		while (i != len2) {
			Posting p2 = literalPostings2.get(i);
			if(hashsetPosting.contains(p2.getDocumentId())){
				result.add(new Posting(p2.getDocumentId()));
			}
			i++;
		}

		return result;
	}

	private List<Posting> intersectPostingDocumentIds(List<Posting> literalPostings1, List<Posting> literalPostings2) {
		List<Posting> result = new ArrayList<Posting>();
		int len1 = literalPostings1.size();
		int len2 = literalPostings2.size();
		int i = 0;
		int j = 0;
		while (i != len1 && j != len2) {
			Posting p1 = literalPostings1.get(i);
			Posting p2 = literalPostings2.get(j);

			if (p1.getDocumentId() == p2.getDocumentId()) {
				result.add(new Posting(p1.getDocumentId()));
				i++;
				j++;
			} else if (p1.getDocumentId() < p2.getDocumentId()) {
				i++;
			} else {
				j++;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
