package cecs429.querying;

import java.util.ArrayList;
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
		if (mComponents.size() == 0) {
			return result;
		}

		result = mComponents.get(0).getPostings(index);
		for (int i = 1; i < mComponents.size(); i++) {
			List<Posting> postingList1 = mComponents.get(i).getPostings(index);
			result = intersectPostingDocumentIds(result, postingList1);
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