package cecs429.querying;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

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
		List<Posting> result = new ArrayList<>();
		if (mComponents.size() == 0) {
			return result;
		}

		result = mComponents.get(0).getPostings(index);

		for (int i = 1; i < mComponents.size(); i++) {
			List<Posting> postingList1 = mComponents.get(i).getPostings(index);
			result = unionPostingDocumentIds(result, postingList1);
		}

		return result;
	}

	private List<Posting> unionPostingDocumentIds(List<Posting> literalPostings1, List<Posting> literalPostings2) {
		List<Posting> result = new ArrayList<Posting>();
		int len1 = literalPostings1.size();
		int len2 = literalPostings2.size();
		int i = 0;
		int j = 0;
		while (i != len1 && j != len2) {
			int docId1 = literalPostings1.get(i).getDocumentId();
			int docId2 = literalPostings2.get(j).getDocumentId();

			int docIdToBeInserted = 0;
			List<Integer> positionsToBeInserted = null;
			if (docId1 < docId2) {
				// Insert docId1 in result
				docIdToBeInserted = docId1;
				positionsToBeInserted = literalPostings1.get(i).getPositions();
				i++;
			} else {
				// Insert docId2 in result
				docIdToBeInserted = docId2;
				positionsToBeInserted = literalPostings2.get(j).getPositions();
				j++;
			}

			// If the last inserted document id is same don't insert it into the list
			if (result.size() == 0 || result.get(result.size() - 1).getDocumentId() != docIdToBeInserted) {
				result.add(new Posting(docIdToBeInserted, positionsToBeInserted));
			}
		}

		while (i < len1) {
			int docIdToBeInserted = literalPostings1.get(i).getDocumentId();
			List<Integer> positionsToBeInserted = literalPostings1.get(i).getPositions();
			result.add(new Posting(docIdToBeInserted, positionsToBeInserted));
			i++;
		}

		while (j < len2) {
			int docIdToBeInserted = literalPostings2.get(j).getDocumentId();
			List<Integer> positionsToBeInserted = literalPostings2.get(j).getPositions();
			result.add(new Posting(docIdToBeInserted, positionsToBeInserted));
			j++;
		}

		return result;
	}

	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
				String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
				+ " )";
	}
}
