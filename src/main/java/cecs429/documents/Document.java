package cecs429.documents;

import java.io.Reader;

/**
 * Represents a document in an index.
 */
public interface Document {
	/**
	 * The ID used by the index to represent the document.
	 */
	int getId();

	/**
	 * Gets a stream over the content of the document.
	 */
	Reader getContent();

	/**
	 * The title of the document, for displaying to the user.
	 */
	String getTitle();

	/**
	 * The file name of the document, for displaying to the user.
	 */
	String getDocumentName();

	/**
	 * The size of the document in bytes.
	 */
	long getByteSize();
}
