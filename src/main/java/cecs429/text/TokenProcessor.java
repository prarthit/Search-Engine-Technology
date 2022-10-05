package cecs429.text;

import java.util.List;

/**
 * A TokenProcessor applies some rules of normalization to a token from a
 * document, and returns a term for that token.
 */
public interface TokenProcessor {
	/**
	 * Normalizes a token into a list of terms.
	 */
	List<String> processToken(String token);

	/**
	 * Normalizes a search query.
	 */
	String processQuery(String token);
}
