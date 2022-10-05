package cecs429.querying;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import cecs429.indexing.Index;
import cecs429.indexing.KGramIndex;
import cecs429.indexing.Posting;
import cecs429.text.TokenProcessor;

/**
 * A wildcard literal
 */
public class WildcardLiteral implements QueryComponent {
    private String mTerm;
    private KGramIndex mKGramIndex;
    private TokenProcessor mTokenProcessor;

    public WildcardLiteral(String term, TokenProcessor tokenProcessor, KGramIndex kGramIndex) {
        mTerm = term;
        mKGramIndex = kGramIndex;
        mTokenProcessor = tokenProcessor;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        // Process the wildcard query by transforming it to lowercase
        mTerm = mTerm.toLowerCase();

        List<String> termsMatchingWildcard = findWordsMatchingWildcard();
        System.out.println("Terms matching the wildcard: " + termsMatchingWildcard);

        // For the final list of words, find the postings and merge them
        List<QueryComponent> termLiterals = new ArrayList<>();
        termsMatchingWildcard.forEach(term -> {
            termLiterals.add(new TermLiteral(term, mTokenProcessor));
        });
        QueryComponent orQueryComponent = new OrQuery(termLiterals);

        return orQueryComponent.getPostings(index);
    }

    // Generate largest possible k-gram terms from a single term
    // For example washi*t*n becomes [$wa, was, ash, shi, t, n$]
    private List<String> genLargestKGrams(String term) {
        term = '$' + term + '$';
        int k = mKGramIndex.getK();
        List<String> largestKGramTerms = new ArrayList<>();

        String[] tokens = term.split("\\*");
        for (String token : tokens) {
            for (int i = 0; i < token.length() - k + 1; i++) {
                largestKGramTerms.add((String) token.subSequence(i, i + k));
            }
            if (token.length() < k)
                largestKGramTerms.add(token);

            // Remove the single '$' from 1-grams
            largestKGramTerms.remove("$");
        }

        return largestKGramTerms;
    }

    // Predicate logic to check if the word can be deduced to wildcard
    private Predicate<? super String> wordDoesnotMatchWildCard = word -> {
        final String wildCardTerm = mTerm;
        int len1 = wildCardTerm.length(), len2 = word.length();

        Boolean[][] dp = new Boolean[len1][len2];
        for (int i = 0; i < len1; i++) {
            for (int j = 0; j < len2; j++) {
                dp[i][j] = false;
            }
        }

        if (wildCardTerm.charAt(0) == word.charAt(0))
            dp[0][0] = true;

        if (wildCardTerm.charAt(0) == '*') {
            for (int j = 0; j < len2; j++) {
                dp[0][j] = true;
            }

            if (len1 > 1) {
                dp[1][0] = wildCardTerm.charAt(1) == word.charAt(0);
            }
        }

        if (len1 > 1 && wildCardTerm.charAt(1) == '*' && dp[0][0]) {
            dp[1][0] = true;
        }

        for (int i = 1; i < len1; i++) {
            for (int j = 1; j < len2; j++) {
                if (wildCardTerm.charAt(i) == '*') {
                    dp[i][j] = dp[i - 1][j] || dp[i][j - 1];
                } else {
                    dp[i][j] = dp[i - 1][j - 1] && wildCardTerm.charAt(i) == word.charAt(j);
                }
            }
        }

        return !dp[len1 - 1][len2 - 1];
    };

    // Find all the words matching the wildcard term
    private List<String> findWordsMatchingWildcard() {
        String wildCardTerm = mTerm;

        // Break the term into largest k-grams possible
        List<String> largestKGramTerms = genLargestKGrams(wildCardTerm);

        List<String> commonWords = mKGramIndex.getWordsContainingAllKGrams(largestKGramTerms);

        // Post filtering to remove terms not matching the wildcard
        commonWords.removeIf(wordDoesnotMatchWildCard);

        return commonWords;
    }

    @Override
    public String toString() {
        return mTerm;
    }
}
