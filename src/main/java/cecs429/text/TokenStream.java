package cecs429.text;

import java.io.Closeable;
import java.io.IOException;

/**
 * Creates a sequence of String tokens from the contents of another stream,
 * breaking the bytes of the stream into tokens
 * in some way.
 */
public interface TokenStream extends Closeable {
	/**
	 * Gets a sequence of tokens from a stream that can be iterated over.
	 */
	Iterable<String> getTokens();

	/**
	 * Closes the stream once it has been consumed, freeing the underlying content
	 * stream.
	 */
	void close() throws IOException;
}
