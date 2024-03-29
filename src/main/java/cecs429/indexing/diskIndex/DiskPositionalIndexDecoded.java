package cecs429.indexing.diskIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
public class DiskPositionalIndexDecoded implements Index {
    private RandomAccessFile postings;
    private TermPositionCrud termPositionCrud;
    private HashMap<String, Long> cache = new HashMap<>();
    
    /**
     * Create a disk positional inverted index.
     * 
     * @param diskDirectoryPath diskDirectoryPath of where disk indexes can be found
     * @throws SQLException
     */
    public DiskPositionalIndexDecoded(String diskDirectoryPath) throws SQLException {
        try {
            postings = new RandomAccessFile(
                    new File(diskDirectoryPath + DiskIndexEnum.POSITIONAL_INDEX_ENCODED.getIndexFileName()), "r");
            termPositionCrud = new TermPositionCrud(DiskIndexEnum.POSITIONAL_INDEX_ENCODED.getDbIndexFileName());
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
            VariableByteCode vbCode = new VariableByteCode();

            postings.seek(bytePosition);

            int documentFrequency = vbCode.decodeNumber(postings);

            int lastDocId = 0;

            for (int i = 0; i < documentFrequency; i++) {

                int docId = vbCode.decodeNumber(postings) + lastDocId;

                // number of times term occurs in the doc
                int termFrequency = vbCode.decodeNumber(postings);
                int[] positions = new int[termFrequency];

                int lastPositionGap = 0;
                for (int positionIndex = 0; positionIndex < termFrequency; positionIndex++) {
                    // (current position + last position) <-> position gaps
                    positions[positionIndex] = vbCode.decodeNumber(postings) + lastPositionGap;

                    lastPositionGap = positions[positionIndex];
                }

                lastDocId = docId;

                Posting Posting = new Posting(docId, Arrays.stream(positions).boxed().collect(Collectors.toList()));
                docIds.add(Posting);
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

            long bytePosition = 0;
            if(cache.get(term)!=null){
                bytePosition = cache.get(term);
            }
            else{
                bytePosition = termPositionCrud.getBytePositionFromModel(term);
                cache.put(term, cache.getOrDefault(term, bytePosition));
            }

            if (bytePosition == -1) {
                return docIds;
            }

            VariableByteCode vbCode = new VariableByteCode();

            // Using the already-opened postings.bin file, seek to the position of the term
            postings.seek(bytePosition);

            int documentFrequency = vbCode.decodeNumber(postings);
            int docId = 0;
            int lastDocId = 0;

            for (int i = 0; i < documentFrequency; i++) {

                // (docId + lastDocId) <-> doc id gaps
                docId = vbCode.decodeNumber(postings) + lastDocId;

                int termFrequency = vbCode.decodeNumber(postings);

                // Scan through the positions of the term, as we only care about the docIds
                for (int positionIndex = 0; positionIndex < termFrequency; positionIndex++) {
                    vbCode.decodeNumber(postings);
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