package cecs429.indexing.diskIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.indexing.database.TermPositionCrud;
import cecs429.indexing.database.TermPositionModel;
import utils.Utils;

/**
 * A DiskBiwordIndex can retrieve postings for a biword term from a data
 * structure
 * associating terms and the documents
 * that contain them.
 */
public class DiskBiwordIndex implements Index {
    private RandomAccessFile postings;
    private TermPositionCrud termPositionCrud;

    /**
     * Create a disk positional inverted index.
     * 
     * @param diskDirectoryPath diskDirectoryPath of where disk indexes can be found
     * @throws SQLException
     */
    public DiskBiwordIndex(String diskDirectoryPath) throws SQLException {
        try {
            postings = new RandomAccessFile(
                    new File(diskDirectoryPath + DiskIndexEnum.BIWORD_INDEX.getIndexFileName()), "r");

            termPositionCrud = new TermPositionCrud(DiskIndexEnum.BIWORD_INDEX.getDbIndexFileName());
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

            for (int i = 0; i < documentFrequency; i++) {
                postings.read(docIdsByteBuffer, 0, docIdsByteBuffer.length);

                // (docId + lastDocId) <-> doc id gaps
                docId = ByteBuffer.wrap(docIdsByteBuffer).getInt() + lastDocId;
                lastDocId = docId;

                docIds.add(new Posting(docId));
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

    @Override
    public List<Posting> getPostingsExcludePositions(String term) {
        return null;
    }

}