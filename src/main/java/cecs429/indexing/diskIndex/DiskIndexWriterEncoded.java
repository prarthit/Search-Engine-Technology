package cecs429.indexing.diskIndex;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.indexing.database.TermPositionCrud;
import cecs429.indexing.database.TermPositionModel;

public class DiskIndexWriterEncoded {
    private Index positionalDiskIndex;
    private String diskDirectoryPath;

    TermPositionCrud termPositionCrud;
    TermPositionModel termPositionModel;

    private final int MAXIMUM_BATCH_LIMIT = 1000;

    public DiskIndexWriterEncoded(Index positionalDiskIndex, String diskDirectoryPath) throws SQLException {
        this.positionalDiskIndex = positionalDiskIndex;
        this.diskDirectoryPath = diskDirectoryPath;
    }

    public void writeIndex() throws SQLException {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Compression Disk Indexing...");

            termPositionCrud = new TermPositionCrud(DiskIndexEnum.POSITIONAL_INDEX_ENCODED.getDbIndexFileName());
            termPositionCrud.openConnection();
            termPositionCrud.createTable();

            termPositionModel = new TermPositionModel();
            VariableByteCode vbCode = new VariableByteCode();

            RandomAccessFile raf = new RandomAccessFile(
                    diskDirectoryPath + DiskIndexEnum.POSITIONAL_INDEX_ENCODED.getIndexFileName(), "rw");
            raf.seek(0);

            List<String> vocab = positionalDiskIndex.getVocabulary();
            int vocabCount = 0;
            int flag = 0;
            for (String term : vocab) {

                if (vocabCount % MAXIMUM_BATCH_LIMIT == 0) {
                    if (flag == 1)
                        termPositionCrud.executeInsertBatch();
                    flag = 1;
                    termPositionCrud.initializePreparestatement();
                } else
                    termPositionCrud.add(term, raf.getChannel().position());

                List<Posting> postings = positionalDiskIndex.getPostings(term);
                byte[] docFreqBytes = vbCode.encodeNumber(postings.size());
                raf.write(docFreqBytes, 0, docFreqBytes.length);

                int lastDocId = 0;
                for (Posting p : postings) {
                    int docId = p.getDocumentId();

                    // docId - lastDocId includes a gap
                    byte[] docIdBytes = vbCode.encodeNumber(docId - lastDocId);

                    raf.write(docIdBytes, 0, docIdBytes.length);

                    List<Integer> positions = p.getPositions();
                    int termFrequency = positions.size();
                    byte[] PosFreqBytes = vbCode.encodeNumber(termFrequency);
                    raf.write(PosFreqBytes, 0, PosFreqBytes.length);

                    int lastPos = 0;
                    for (int pos : positions) {
                        byte[] posIdBytes = vbCode.encodeNumber(pos - lastPos);
                        raf.write(posIdBytes, 0, posIdBytes.length);
                        lastPos = pos;
                    }
                    lastDocId = docId;
                }
                ++vocabCount;
            }

            termPositionCrud.executeInsertBatch();
            termPositionCrud.sqlCommit();

            long endTime = System.currentTimeMillis(); // End time to build positional Inverted Index

            System.out.println("Time taken to write encoded disk positional index: " + ((endTime - startTime) / 1000)
                    + " seconds");

            raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
