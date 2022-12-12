package cecs429.querying;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	private KGramIndex mKGramIndex;
	private Index mBiwordIndex;
	private TokenProcessor mTokenProcessor;

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms
	 * separated by spaces.
	 */
	public PhraseLiteral(String terms, TokenProcessor tokenProcessor, KGramIndex kGramIndex, Index biwordIndex) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
		mKGramIndex = kGramIndex;
		mBiwordIndex = biwordIndex;
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
		if (mTerms.size() == 2 && mBiwordIndex != null) {
			String query1 = mTerms.get(0);
			String query2 = mTerms.get(1);

			// If either of the both queries contains a wildcard,
			// don't process it as a biword query
			if (!query1.contains("*") && !query2.contains("*")) {
				String processedQuery1 = mTokenProcessor.processQuery(query1);
				String processedQuery2 = mTokenProcessor.processQuery(query2);

				return mBiwordIndex.getPostings(processedQuery1 + " " + processedQuery2);
			}
		}

		boolean impact_ordering = false;
		// Convert raw query string into a term or wildcard literal
		QueryComponent currQueryComponent = termToLiteral(mTerms.get(0));
		List<Posting> result = currQueryComponent.getPostings(index);

		if (!utils.Utils.isSortedList(result)) {
			impact_ordering = true;
		}

		for (int i = 1; i < mTerms.size(); i++) {
			// Convert raw query string into a term or wildcard literal
			currQueryComponent = termToLiteral(mTerms.get(i));
			List<Posting> literalPostings = currQueryComponent.getPostings(index);
			if (impact_ordering || !utils.Utils.isSortedList(literalPostings)) {
				result = positionalIntersectUnsortedLists(result, literalPostings);
			} else
				result = positionalIntersect(result, literalPostings);
		}

		return result;
	}

	private List<Posting> positionalIntersectUnsortedLists(List<Posting> literalPostings1,
			List<Posting> literalPostings2) {
		List<Posting> res = new ArrayList<>();
		HashMap<Integer, List<Integer>> hmap = new HashMap<>();

		if (literalPostings1.size() > literalPostings2.size()) {
			createHashMapWithPostings(hmap, literalPostings1);
			findPostingUsingHashMap(res, hmap, literalPostings2);
		} else {
			createHashMapWithPostings(hmap, literalPostings2);
			findPostingUsingHashMap(res, hmap, literalPostings1);
		}

		return res;
	}

	private void createHashMapWithPostings(HashMap<Integer, List<Integer>> hmap, List<Posting> literalPostings) {
		for (Posting p : literalPostings) {
			hmap.put(p.getDocumentId(), p.getPositions());
		}
	}

	private void findPostingUsingHashMap(List<Posting> res, HashMap<Integer, List<Integer>> hmap,
			List<Posting> literalPostings) {
		for (Posting p : literalPostings) {
			if (hmap.containsKey(p.getDocumentId())) {
				List<Integer> documentPositions = new ArrayList<>();
				List<Integer> postingPosition1 = p.getPositions();
				List<Integer> postingPosition2 = hmap.get(p.getDocumentId());

				mergePostingPositions(documentPositions, postingPosition1, postingPosition2);
				if (documentPositions.size() != 0) {
					res.add(new Posting(p.getDocumentId(), documentPositions));
				}
			}
		}
	}

	private void mergePostingPositions(List<Integer> documentPositions, List<Integer> postingPosition1,
			List<Integer> postingPosition2) {
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

				mergePostingPositions(documentPositions, postingPosition1, postingPosition2);
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
