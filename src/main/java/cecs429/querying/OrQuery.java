package cecs429.querying;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.AdvancedTokenProcessor;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a
 * union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;

	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}

	@Override
	public List<Posting> getPostings(Index index) {
		if (mComponents.size() == 0) {
			return null;
		}

		List<Posting> result = mComponents.get(0).getPostings(index);

		for (int i = 1; i < mComponents.size(); i++) {
			List<Posting> postingList1 = mComponents.get(i).getPostings(index);
			result = intersectPostingDocumentIds(result, postingList1);
		}

		return result;
	}

	private List<Posting> intersectPostingDocumentIds(List<Posting> literalPostings1, List<Posting> literalPostings2) {
		literalPostings1.addAll(literalPostings2);

		HashSet<Integer> seen = new HashSet<>();
		literalPostings1.removeIf(e -> !seen.add(e.getDocumentId()));

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
