package cecs429.querying;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.KGramIndex;
import cecs429.indexing.Posting;
import cecs429.text.TokenProcessor;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class NearLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	private Integer kNear = 1;
	private KGramIndex mKGramIndex;
	private TokenProcessor mTokenProcessor;

	/**
	 * Constructs a NearLiteral with the given list of terms.
	 */
	public NearLiteral(List<String> terms, TokenProcessor tokenProcessor, KGramIndex kGramIndex) {
		mTerms.addAll(terms);
		mKGramIndex = kGramIndex;
		mTokenProcessor = tokenProcessor;
	}

	// Convert a string query to a QueryComponent
	private QueryComponent termToLiteral(String term) {
		QueryComponent queryComponent;
		if (term.contains("*") && mKGramIndex != null) {
			queryComponent = new WildcardLiteral(term, mTokenProcessor, mKGramIndex);
		} else {
			queryComponent = new TermLiteral(term, mTokenProcessor);
		}
		return queryComponent;
	}

	@Override
	public List<Posting> getPostings(Index index) {
		// because our terms list contains term1, kNear and term2
		if (mTerms.size() < 3) {
			return new ArrayList<>();
		}

		QueryComponent firstLiteral = termToLiteral(mTerms.get(0));
		List<Posting> result = firstLiteral.getPostings(index);

		for (int i = 2; i < mTerms.size(); i += 2) {
			kNear = Integer.parseInt(mTerms.get(i - 1));
			List<Posting> literalPostings = termToLiteral(mTerms.get(i)).getPostings(index);
			result = positionalIntersect(result, literalPostings, kNear);
		}

		return result;
	}

	private List<Posting> positionalIntersect(List<Posting> literalPostings1, List<Posting> literalPostings2,
			Integer kNear) {
		List<Posting> res = new ArrayList<>();
		int len1 = literalPostings1.size();
		int len2 = literalPostings2.size();
		int i = 0;
		int j = 0;
		while (i != len1 && j != len2) {
			Posting p1 = literalPostings1.get(i);
			Posting p2 = literalPostings2.get(j);

			if (p1.getDocumentId() == p2.getDocumentId()) {
				List<Integer> documentPositions = new ArrayList<>();
				List<Integer> postingPosition1 = p1.getPositions();
				List<Integer> postingPosition2 = p2.getPositions();

				int plen1 = postingPosition1.size();
				int plen2 = postingPosition2.size();

				int m = 0;
				int n = 0;
				while (m != plen1) {
					int mPosition = postingPosition1.get(m);
					while (n != plen2) {
						int nPosition = postingPosition2.get(n);
						if (nPosition - mPosition > 0 && nPosition - mPosition <= kNear) {
							documentPositions.add(nPosition);
							n++;
						} else if (nPosition <= mPosition) {
							n++;
						} else {
							break;
						}
					}
					m++;
				}
				if (documentPositions.size() != 0) {
					res.add(new Posting(p1.getDocumentId(), documentPositions));
				}
				i++;
				j++;
			} else if (p1.getDocumentId() < p2.getDocumentId()) {
				i++;
			} else {
				j++;
			}
		}
		return res;
	}

	@Override
	public String toString() {
		return "["
				+ String.join(" NEAR/ " + kNear, mTerms.stream().map(c -> c.toString()).collect(Collectors.toList()))
				+ "]";
	}
}
