package cecs429.querying;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.AdvancedTokenProcessor;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	Index _biwordIndex = null;

	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms, Index biwordIndex) {
		mTerms.addAll(terms);
		_biwordIndex = biwordIndex;
	}

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms
	 * separated by spaces.
	 */
	public PhraseLiteral(String terms, Index biwordIndex) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
		_biwordIndex = biwordIndex;
	}

	@Override
	public List<Posting> getPostings(Index index) {
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

		if (mTerms.size() == 2) {
			return _biwordIndex.getPostings(
					processor.processQuery(mTerms.get(0)) + " " + processor.processQuery(mTerms.get(1)));
		}

		String processedQuery = processor.processQuery(mTerms.get(0));
		List<Posting> result = index.getPostings(processedQuery);

		for (int i = 1; i < mTerms.size(); i++) {
			List<Posting> literalPostings = index.getPostings(processor.processQuery(mTerms.get(i)));
			result = positionalIntersect(result, literalPostings);
		}

		return result;
	}

	private List<Posting> positionalIntersect(List<Posting> literalPostings1, List<Posting> literalPostings2) {
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

				while (m != plen1 && n != plen2) {
					int mPosition = postingPosition1.get(m);
					int nPosition = postingPosition2.get(n);

					if (nPosition - mPosition == 1) {
						documentPositions.add(nPosition);
						m++;
						n++;
					} else if (mPosition >= nPosition) {
						n++;
					} else {
						m++;
					}
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
		String terms = mTerms.stream()
				.collect(Collectors.joining(" "));
		return "\"" + terms + "\"";
	}
}
