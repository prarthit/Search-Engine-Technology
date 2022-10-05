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

	private void addPostingToResult(List<Posting> result, Posting p) {
		// If the last inserted document id is same don't insert it into the list
		if (result.size() == 0 || result.get(result.size() - 1).getDocumentId() != p.getDocumentId()) {
			result.add(p);
		}
		// Else if the document id is the same, only insert the positions
		else if (result.get(result.size() - 1).getDocumentId() == p.getDocumentId()) {
			List<Integer> newPositions = new ArrayList<>();

			List<Integer> positions1 = result.get(result.size() - 1).getPositions();
			List<Integer> positions2 = p.getPositions();

			int i = 0, j = 0;
			int len1 = positions1.size(), len2 = positions2.size();
			while (i < len1 && j < len2) {
				if (positions1.get(i) == positions2.get(j)) {
					newPositions.add(positions2.get(j));
					i++;
					j++;
				} else if (positions1.get(i) < positions2.get(j)) {
					newPositions.add(positions1.get(i));
					i++;
				} else {
					newPositions.add(positions2.get(j));
					j++;
				}
			}

			while (i < len1) {
				newPositions.add(positions1.get(i));
				i++;
			}

			while (j < len2) {
				newPositions.add(positions2.get(j));
				j++;
			}

			result.set(result.size() - 1, new Posting(p.getDocumentId(), newPositions));
		}

		return;
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

			Posting postingToBeInserted = null;
			if (docId1 < docId2) {
				// Insert posting from literalPostings1 in result
				postingToBeInserted = literalPostings1.get(i);
				i++;
			} else {
				// Insert posting from literalPostings2 in result
				postingToBeInserted = literalPostings2.get(j);
				j++;
			}

			addPostingToResult(result, postingToBeInserted);
		}

		while (i < len1) {
			Posting postingToBeInserted = literalPostings1.get(i);
			addPostingToResult(result, postingToBeInserted);
			i++;
		}

		while (j < len2) {
			Posting postingToBeInserted = literalPostings2.get(j);
			addPostingToResult(result, postingToBeInserted);
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
