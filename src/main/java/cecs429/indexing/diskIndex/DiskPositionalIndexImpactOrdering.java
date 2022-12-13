package cecs429.indexing.diskIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.indexing.database.TermPositionCrud;
import cecs429.indexing.database.TermPositionModel;

/**
 * A DiskPositionalIndex can retrieve postings for a term from a data structure
 * associating terms and the documents
 * that contain them.
 */
public class DiskPositionalIndexImpactOrdering implements Index {
    private RandomAccessFile postings;
    private TermPositionCrud termPositionCrud;

    /**
     * Create a disk positional inverted index.
     * 
     * @param diskDirectoryPath diskDirectoryPath of where disk indexes can be found
     * @throws SQLException
     */
    public DiskPositionalIndexImpactOrdering(String diskDirectoryPath) throws SQLException {
        try {
            postings = new RandomAccessFile(
                    new File(diskDirectoryPath + DiskIndexEnum.POSITIONAL_INDEX_IMPACT.getIndexFileName()), "r");
            termPositionCrud = new TermPositionCrud(DiskIndexEnum.POSITIONAL_INDEX_IMPACT.getDbIndexFileName());

            termPositionCrud.openConnection();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves a list of Postings of documents that contain the given term with
     * positions
     */
    @Override
    public List<Posting> getPostings(String term) {
        try {
            List<Posting> docIds = new ArrayList<Posting>();

            TermPositionModel termPositionModel = termPositionCrud.getTermPositionModel(term);
            if (termPositionModel == null) {
                return docIds;
            }

            long bytePosition = termPositionModel.getBytePosition();

            // Using the already-opened postings.bin file, seek to the position of the term
            postings.seek(bytePosition);

            byte[] buffer = new byte[4];
            postings.read(buffer, 0, buffer.length);

            int documentFrequency = ByteBuffer.wrap(buffer).getInt();

            int docId = 0, lastDocId = 0;

            byte docIdsByteBuffer[] = new byte[4];
            byte positionsByteBuffer[] = new byte[4];

            for (int i = 0; i < documentFrequency; i++) {

                // Reads the document Id into docIdsByteBuffer
                postings.read(docIdsByteBuffer, 0, docIdsByteBuffer.length);

                // (docId + lastDocId) <-> doc id gaps
                docId = ByteBuffer.wrap(docIdsByteBuffer).getInt() + lastDocId;
                buffer = new byte[4];

                postings.read(buffer, 0, buffer.length);

                // number of times term occurs in the doc
                int termFrequency = ByteBuffer.wrap(buffer).getInt();
                int[] positions = new int[termFrequency];

                int lastPositionGap = 0;
                for (int positionIndex = 0; positionIndex < termFrequency; positionIndex++) {
                    postings.read(positionsByteBuffer, 0, positionsByteBuffer.length);

                    // (current position + last position) <-> position gaps
                    positions[positionIndex] = ByteBuffer.wrap(positionsByteBuffer).getInt() + lastPositionGap;
                    lastPositionGap = positions[positionIndex];
                }

                lastDocId = docId;

                Posting p = new Posting(docId, Arrays.stream(positions).boxed().collect(Collectors.toList()));
                docIds.add(p);
            }

            return docIds;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a list of Postings of documents that contain the given term without
     * positions
     */
    public List<Posting> getPostingsExcludePositions(String term) {
        try {
            List<Posting> docIds = new ArrayList<Posting>();

            TermPositionModel termPositionModel = termPositionCrud.getTermPositionModel(term);
            if (termPositionModel == null) {
                return docIds;
            }

            long bytePosition = termPositionModel.getBytePosition();

            // Using the already-opened postings.bin file, seek to the position of the term
            postings.seek(bytePosition);

            byte[] buffer = new byte[4];
            postings.read(buffer, 0, buffer.length);

            int documentFrequency = ByteBuffer.wrap(buffer).getInt();

            int docId = 0;
            int lastDocId = 0;

            byte docIdsByteBuffer[] = new byte[4];
            byte positionsByteBuffer[] = new byte[4];

            for (int i = 0; i < documentFrequency; i++) {

                // Reads the document Id into docIdsByteBuffer
                postings.read(docIdsByteBuffer, 0, docIdsByteBuffer.length);

                // (docId + lastDocId) <-> doc id gaps
                docId = ByteBuffer.wrap(docIdsByteBuffer).getInt() + lastDocId;
                buffer = new byte[4];

                postings.read(buffer, 0, buffer.length);
                int termFrequency = ByteBuffer.wrap(buffer).getInt();

                // Scan through the positions of the term, as we only care about the docIds
                for (int positionIndex = 0; positionIndex < termFrequency; positionIndex++) {
                    postings.read(positionsByteBuffer, 0, positionsByteBuffer.length);
                }

                lastDocId = docId;

                Posting p = new Posting(docId);
                p.setTermFrequency(termFrequency);

                docIds.add(p);
            }

            return docIds;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Read a list of all vocabs from disk
     * 
     * @return list of all vocabs
     */
    public List<String> getVocabulary() {
        try {
            List<String> vocabulary = termPositionCrud.getVocabularyTerm();
            return vocabulary;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}