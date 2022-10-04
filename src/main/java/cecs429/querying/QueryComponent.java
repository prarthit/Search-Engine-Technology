package cecs429.querying;
import java.util.List;

import cecs429.indexing.*;

/**
 * A QueryComponent is one piece of a larger query, whether that piece is a
 * literal string or represents a merging of
 * other components. All nodes in a query parse tree are QueryComponent objects.
 */
public interface QueryComponent {
    /**
     * Retrieves a list of postings for the query component, using an Index as the
     * source.
     */
    List<Posting> getPostings(Index index);
    // Checks whether query component is negative i.e NOT query
    // boolean isNegative();
}