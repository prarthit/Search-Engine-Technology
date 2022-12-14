package cecs429.indexing.diskIndex;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.List;
import java.util.PriorityQueue;

import cecs429.documents.DocumentCorpus;
import cecs429.indexing.BiwordInvertedIndex;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.indexing.database.TermPositionCrud;
import cecs429.indexing.database.TermPositionModel;
import cecs429.text.TokenProcessor;
import utils.Utils;

public class DiskIndexWriter {
    private Index positionalInvertedIndex;
    private Index biwordInvertedIndex;
    private String diskDirectoryPath;

    private int maxBatchLimit;

    TermPositionCrud termPositionCrud;
    TermPositionModel termPositionModel;

    TokenProcessor processor;
    DocumentCorpus corpus;

    public DiskIndexWriter() {
    }

    public void setMetrics(String diskDirectoryPath, TokenProcessor processor, DocumentCorpus corpus){
        this.diskDirectoryPath = diskDirectoryPath;
        this.processor = processor;
        this.corpus = corpus;

    }

    public void setPositionalIndex() {
        if(positionalInvertedIndex == null){
            positionalInvertedIndex = new PositionalInvertedIndex(corpus, processor);
        }
    }

    public void setBiwordIndex() {
        if(biwordInvertedIndex == null){
            biwordInvertedIndex = new BiwordInvertedIndex(corpus, processor);
        }
    }

    public void setMaximumBatchLimit(int maxBatchLimit) {
        this.maxBatchLimit = maxBatchLimit;
    }

    public void writeIndex() throws SQLException {
        try {
            if (Utils.isFileExist(DiskIndexEnum.POSITIONAL_INDEX.getIndexFileName()))
                return;

            setPositionalIndex();

            long startTime = System.currentTimeMillis();
            System.out.println("Disk Indexing...");
            termPositionCrud = new TermPositionCrud(DiskIndexEnum.POSITIONAL_INDEX.getDbIndexFileName());
            termPositionCrud.openConnection();
            termPositionCrud.createTable();

            termPositionModel = new TermPositionModel();

            RandomAccessFile raf = new RandomAccessFile(
                    diskDirectoryPath + DiskIndexEnum.POSITIONAL_INDEX.getIndexFileName(), "rw");
            raf.seek(0);

            List<String> vocab = positionalInvertedIndex.getVocabulary();
            int vocabCount = 0;
            int flag = 0;
            for (String term : vocab) {

                initializeAddExecuteBatch(raf, vocabCount, flag, term);
                flag = 1;

                List<Posting> postings = positionalInvertedIndex.getPostings(term);
                byte[] docFreqterm = ByteBuffer.allocate(4).putInt(postings.size()).array();
                raf.write(docFreqterm, 0, docFreqterm.length);

                int lastDocId = 0;
                for (Posting p : postings) {
                    int docId = p.getDocumentId();

                    // docId - lastDocId includes a gap
                    byte[] docIdBytes = ByteBuffer.allocate(4).putInt(docId - lastDocId).array();
                    raf.write(docIdBytes, 0, docIdBytes.length);

                    List<Integer> positions = p.getPositions();
                    int termFrequency = positions.size();
                    byte[] termFreqBytes = ByteBuffer.allocate(4).putInt(termFrequency).array();
                    raf.write(termFreqBytes);

                    int lastPos = 0;
                    for (int pos : positions) {
                        byte[] posBytes = ByteBuffer.allocate(4).putInt(pos - lastPos).array();
                        raf.write(posBytes, 0, posBytes.length);
                        lastPos = pos;
                    }
                    lastDocId = docId;
                }
                ++vocabCount;
            }

            termPositionCrud.executeInsertBatch();
            termPositionCrud.sqlCommit();

            long endTime = System.currentTimeMillis(); // End time to build positional Inverted Index

            System.out.println("Time taken to write disk positional index: " + ((endTime - startTime) / 1000)
                    + " seconds");

            raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeBiwordIndex() throws SQLException {
        try {
            if (Utils.isFileExist(DiskIndexEnum.BIWORD_INDEX.getIndexFileName()))
                return;

            setBiwordIndex();

            long startTime = System.currentTimeMillis();
            System.out.println("Biword Disk Indexing...");

            termPositionCrud = new TermPositionCrud(DiskIndexEnum.BIWORD_INDEX.getDbIndexFileName());

            termPositionCrud.openConnection();
            termPositionCrud.createTable();

            termPositionModel = new TermPositionModel();

            RandomAccessFile raf = new RandomAccessFile(
                    diskDirectoryPath + DiskIndexEnum.BIWORD_INDEX.getIndexFileName(), "rw");
            raf.seek(0);

            List<String> vocab = biwordInvertedIndex.getVocabulary();
            int vocabCount = 0;
            int flag = 0;
            for (String term : vocab) {

                initializeAddExecuteBatch(raf, vocabCount, flag, term);
                flag = 1;

                List<Posting> postings = biwordInvertedIndex.getPostings(term);
                byte[] docFreqterm = ByteBuffer.allocate(4).putInt(postings.size()).array();
                raf.write(docFreqterm, 0, docFreqterm.length);

                int lastDocId = 0;
                for (Posting p : postings) {
                    int docId = p.getDocumentId();

                    // docId - lastDocId includes a gap
                    byte[] docIdBytes = ByteBuffer.allocate(4).putInt(docId - lastDocId).array();
                    raf.write(docIdBytes, 0, docIdBytes.length);

                    lastDocId = docId;
                }
                ++vocabCount;
            }

            termPositionCrud.executeInsertBatch();
            termPositionCrud.sqlCommit();

            long endTime = System.currentTimeMillis(); // End time to build positional Inverted Index

            System.out.println("Time taken to write disk biword index: " + ((endTime - startTime) / 1000)
                    + " seconds");

            raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeImpactOrderingIndex() throws SQLException {
        try {
            if (Utils.isFileExist(DiskIndexEnum.POSITIONAL_INDEX_IMPACT.getIndexFileName()))
                return;

            setPositionalIndex();

            long startTime = System.currentTimeMillis();
            System.out.println("Impact order Disk Indexing...");
            termPositionCrud = new TermPositionCrud(DiskIndexEnum.POSITIONAL_INDEX_IMPACT.getDbIndexFileName());
            termPositionCrud.openConnection();
            termPositionCrud.createTable();

            termPositionModel = new TermPositionModel();

            RandomAccessFile raf = new RandomAccessFile(
                    diskDirectoryPath + DiskIndexEnum.POSITIONAL_INDEX_IMPACT.getIndexFileName(), "rw");
            raf.seek(0);

            List<String> vocab = positionalInvertedIndex.getVocabulary();
            int vocabCount = 0;
            int flag = 0;
            for (String term : vocab) {

                initializeAddExecuteBatch(raf, vocabCount, flag, term);
                flag = 1;

                PriorityQueue<Posting> pqImpactOrder = new PriorityQueue<Posting>();
                List<Posting> postings = positionalInvertedIndex.getPostings(term);

                for (Posting p : postings) {
                    pqImpactOrder.add(p);
                }

                byte[] docFreqterm = ByteBuffer.allocate(4).putInt(postings.size()).array();
                raf.write(docFreqterm, 0, docFreqterm.length);

                Posting impactPosting = null;

                int lastDocId = 0;
                while (!pqImpactOrder.isEmpty()) {
                    impactPosting = pqImpactOrder.remove();

                    int docId = impactPosting.getDocumentId();

                    // docId - lastDocId includes a gap
                    byte[] docIdBytes = ByteBuffer.allocate(4).putInt(docId - lastDocId).array();
                    raf.write(docIdBytes, 0, docIdBytes.length);

                    List<Integer> positions = impactPosting.getPositions();
                    int termFrequency = positions.size();
                    byte[] termFreqBytes = ByteBuffer.allocate(4).putInt(termFrequency).array();
                    raf.write(termFreqBytes);

                    int lastPos = 0;
                    for (int pos : positions) {
                        byte[] posBytes = ByteBuffer.allocate(4).putInt(pos - lastPos).array();
                        raf.write(posBytes, 0, posBytes.length);
                        lastPos = pos;
                    }
                    lastDocId = docId;
                }
                ++vocabCount;
            }
            termPositionCrud.executeInsertBatch();
            termPositionCrud.sqlCommit();

            long endTime = System.currentTimeMillis(); // End time to build positional Inverted Index

            System.out.println("Time taken to write impact ordering disk index: " + ((endTime - startTime) / 1000)
                    + " seconds");

            raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeIndexEncoded() throws SQLException {
        try {
            if (Utils.isFileExist(DiskIndexEnum.POSITIONAL_INDEX_ENCODED.getIndexFileName()))
                return;

            setPositionalIndex();

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

            List<String> vocab = positionalInvertedIndex.getVocabulary();
            int vocabCount = 0;
            int flag = 0;
            for (String term : vocab) {

                initializeAddExecuteBatch(raf, vocabCount, flag, term);
                flag = 1;
                
                List<Posting> postings = positionalInvertedIndex.getPostings(term);
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

    private void initializeAddExecuteBatch(RandomAccessFile raf, int vocabCount, int flag, String term) {
        try {
            if (vocabCount % maxBatchLimit == 0) {
                if (flag == 1)
                    termPositionCrud.executeInsertBatch();

                termPositionCrud.initializePreparestatement();
            }

            termPositionCrud.add(term, raf.getChannel().position());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
