package cecs429.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/**
 * An AdvancedTokenProcessor creates terms from tokens by removing all
 * non-alphanumeric characters from the beginning and end of token,
 * all apostropes or quotation marks from the token, remove hyphens and
 * split the original hyphenated token into multiple tokens split at hyphen,
 * and stemming & converting it to all lowercase.
 */
public class AdvancedTokenProcessor implements TokenProcessor {
    // Stem the token using Porter2 stemmer algorithm
    private String stemToken(String token) {
        SnowballStemmer snowballStemmer = new englishStemmer();
        snowballStemmer.setCurrent(token);
        snowballStemmer.stem();
        return snowballStemmer.getCurrent();
    }

    public String preProcessToken(String token) {
        // Remove non-alphanumeric characters from beginning of token
        token = token.replaceAll("^[^a-zA-Z0-9]*", "");
        // Remove all apostropes or quotation marks from token
        token = token.replaceAll("[\"'`]", "");
        // Remove non-alphanumeric characters from end of token
        token = token.replaceAll("[^a-zA-Z0-9]*$", "");
        token = token.toLowerCase();

        return token;
    }

    @Override
    public List<String> processToken(String token) {
        List<String> processedTokens = new ArrayList<>();

        token = preProcessToken(token);

        if (token.contains("-")) {
            // Split tokens at hyphen and add them to the tokens list
            List<String> hyphenSubTokens = Arrays.asList(token.split("-"));

            // Stem each token
            hyphenSubTokens = hyphenSubTokens.stream().map(t -> stemToken(t)).toList();

            processedTokens.addAll(hyphenSubTokens);

            // Remove all hyphens from original token
            token = token.replaceAll("-", "");
        }

        // Stem the original token and add to processed tokens
        processedTokens.add(stemToken(token));

        return processedTokens;
    }

    // Process input query and perform all steps similar to processToken except
    // hyphen processing
    public String processQuery(String query) {
        String preProcessedQuery = preProcessToken(query);
        preProcessedQuery = preProcessedQuery.replaceAll("-", ""); // Remove all hyphens
        String stemmedQuery = stemToken(preProcessedQuery);
        return stemmedQuery;
    }

    // Process wildcard query
    public String processWildcardQuery(String query) {
        query = query.toLowerCase();
        return query;
    }
}
