package cecs429.indexing.diskIndex;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.indexing.database.TermPositionCrud;
import cecs429.indexing.database.TermPositionModel;
import cecs429.utils.Utils;

public class DiskIndexWriter {
    private Index positionalDiskIndex;
    private String diskDirectoryPath;

    TermPositionCrud termPositionCrud;
    TermPositionModel termPositionModel;
    
    public DiskIndexWriter(Index positionalDiskIndex, String diskDirectoryPath) throws SQLException{
        this.positionalDiskIndex = positionalDiskIndex;
        this.diskDirectoryPath = diskDirectoryPath;
    }

    public void writeIndex() throws SQLException{
        try{
            termPositionCrud = new TermPositionCrud(Utils.getChildDirectoryName(diskDirectoryPath));
            termPositionCrud.createTable();

            termPositionModel = new TermPositionModel();

            RandomAccessFile raf = new RandomAccessFile(diskDirectoryPath + DiskIndexEnum.POSITIONAL_INDEX.getPostingFileName(), "rw");
            raf.seek(0);

            List<String> vocab = positionalDiskIndex.getVocabulary();
            for(String term : vocab){
                
                termPositionModel.setTerm(term);
                termPositionModel.setBytePosition(raf.getChannel().position());
                termPositionCrud.add(termPositionModel);

                List<Posting> postings = positionalDiskIndex.getPostings(term);
                byte[] docFreqterm = ByteBuffer.allocate(4).putInt(postings.size()).array();
                raf.write(docFreqterm, 0, docFreqterm.length);

                int lastDocId = 0;
                for(Posting p : postings){
                    int docId = p.getDocumentId();

                    // docId - lastDocId includes a gap
                    byte[] docIdBytes = ByteBuffer.allocate(4).putInt(docId - lastDocId).array(); 
					raf.write(docIdBytes, 0, docIdBytes.length);
                    
                    List<Integer> positions = p.getPositions();
                    int termFrequency = positions.size();
                    byte[] termFreqBytes = ByteBuffer.allocate(4).putInt(termFrequency).array();
                    raf.write(termFreqBytes);
                    
                    int lastPos = 0;
                    for(int pos : positions){
                        byte[] posBytes = ByteBuffer.allocate(4).putInt(pos - lastPos).array(); 
						raf.write(posBytes, 0, posBytes.length);
                        lastPos = pos;
                    } 
                    lastDocId = docId;
                }
            }
            raf.close();
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
