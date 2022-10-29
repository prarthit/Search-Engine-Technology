package cecs429.indexing.DiskIndex;

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
import cecs429.indexing.Database.TermPositionCrud;
import cecs429.indexing.Database.TermPositionModel;

/**
 * A DiskPositionalIndex can retrieve postings for a term from a data structure
 * associating terms and the documents
 * that contain them.
 */
public class DiskPositionalIndex implements Index {
    // Need to place in the interface 'index' later - verify
    private RandomAccessFile vocab;
    private RandomAccessFile postings;
    private TermPositionCrud termPositionCrud;
    /**
     * Create a disk positional inverted index.
     * 
     * @param path path of where disk indexes can be found
     * @throws SQLException
     */
    public DiskPositionalIndex(String path) throws SQLException {
        // super();
        try {
            // DiskIndexBuilder diskIndexBuilder = new DiskIndexBuilder(BIWORD_INDEX);
            vocab = new RandomAccessFile(new File(path, DiskIndexEnum.POSITIONAL_INDEX.getVocabFileName()), "r");
            postings = new RandomAccessFile(new File(path, DiskIndexEnum.POSITIONAL_INDEX.getPostingFileName()), "r");
            // mDocWeights = new RandomAccessFile(new File(path, "docWeights.bin"), "r");
            // mVocabTable = readVocabTable(path);
            termPositionCrud = new TermPositionCrud();
        } catch (FileNotFoundException ex) {
            // System.out.println(ex.toString());
            ex.getStackTrace();
        }
    }

    /**
     * Retrieves a list of Postings of documents that contain the given term.
     */
    @Override
    public List<Posting> getPostings(String term) {
        try {
            // initialize the array that will hold the postings.
            List<Posting> docList = new ArrayList<Posting>();

            TermPositionModel termPositionModel = termPositionCrud.getTermPositionModel(term);
            long bytePosition = termPositionModel.getBytePosition();

            // Using the already-opened postings.bin file, seek to the position of the term
            postings.seek(bytePosition);

            byte[] buffer = new byte[4];
            postings.read(buffer, 0, buffer.length);

            int documentFrequency = ByteBuffer.wrap(buffer).getInt();

            int docId = 0;
            int lastDocId = 0;

            byte docIdsBuffer[] = new byte[4];
            byte positionsBuffer[] = new byte[4];
            /// byte wdtBuffer[] = new byte[8];

            for (int docIdIndex = 0; docIdIndex < documentFrequency; docIdIndex++) {

                // Reads the 4 bytes of the docId into docIdsBuffer
                postings.read(docIdsBuffer, 0, docIdsBuffer.length);

                // Convert the byte representation of the docId into the integer
                // representation
                // Current docId is the difference between the lastDocId and the
                // currentDocId
                // So add the lastDocId to the current number read from the
                // postings file to get the currentDocId
                docId = ByteBuffer.wrap(docIdsBuffer).getInt() + lastDocId;

                // Next 8 bytes is the document weight corresponding to the
                // postings.skipBytes(8);
                /// postings.read(wdtBuffer, 0, wdtBuffer.length);
                /// double wdt = ByteBuffer.wrap(wdtBuffer).getDouble();

                // Allocate a buffer for the 4 byte term frequency value
                buffer = new byte[4];

                // Read the term frequency
                postings.read(buffer, 0, buffer.length);
                int termFreq = ByteBuffer.wrap(buffer).getInt();

                // Create a positions list storing the position of each occurence of this term
                // in this document
                int[] positions = new int[termFreq];

                // Iterate through the postings file and get the positions of this term into the
                // positions array
                int lastPositionGap = 0;
                for (int positionIndex = 0; positionIndex < termFreq; positionIndex++) {
                    postings.read(positionsBuffer, 0, positionsBuffer.length);
                    positions[positionIndex] = ByteBuffer.wrap(positionsBuffer).getInt() + lastPositionGap;
                    lastPositionGap = positions[positionIndex];
                }

                lastDocId = docId;

                Posting Posting = new Posting(docId, Arrays.stream(positions).boxed().collect(Collectors.toList()));

                docList.add(Posting);
            }

            return docList;
        } catch (Exception ex) {
            ex.getStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a list of Postings of documents that contain the given term.
     */
    public List<Posting> getPostingsExcludePositions(String term) {
        try {
            // initialize the array that will hold the postings.
            List<Posting> docList = new ArrayList<Posting>();

            long bytePosition = 1;// Get the byte position from the database for the particular term

            // Using the already-opened postings.bin file, seek to the position of the term
            postings.seek(bytePosition);

            byte[] buffer = new byte[4];
            postings.read(buffer, 0, buffer.length);

            int documentFrequency = ByteBuffer.wrap(buffer).getInt();

            int docId = 0;
            int lastDocId = 0;

            byte docIdsBuffer[] = new byte[4];
            /// byte wdtBuffer[] = new byte[8];

            for (int docIdIndex = 0; docIdIndex < documentFrequency; docIdIndex++) {

                // Reads the 4 bytes of the docId into docIdsBuffer
                postings.read(docIdsBuffer, 0, docIdsBuffer.length);

                // Convert the byte representation of the docId into the integer
                // representation
                // Current docId is the difference between the lastDocId and the
                // currentDocId
                // So add the lastDocId to the current number read from the
                // postings file to get the currentDocId
                docId = ByteBuffer.wrap(docIdsBuffer).getInt() + lastDocId;

                // Next 8 bytes is the document weight corresponding to the
                // postings.skipBytes(8);
                /// postings.read(wdtBuffer, 0, wdtBuffer.length);
                /// double wdt = ByteBuffer.wrap(wdtBuffer).getDouble();

                // Allocate a buffer for the 4 byte term frequency value
                buffer = new byte[4];

                // Read the term frequency
                postings.read(buffer, 0, buffer.length);
                lastDocId = docId;

                docList.add(new Posting(docId));
            }

            return docList;
        } catch (Exception ex) {
            ex.getStackTrace();
        }

        return null;
    }

    /**
     * Read a list of all vocabs from disk
     * 
     * @return list of all vocabs
     */
    public List<String> getVocabulary() {
        return null;
    }

}