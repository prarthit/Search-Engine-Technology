package cecs429.indexing;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;

public class DiskIndexWriter {
    private Index positionalDiskIndex;
    private String diskPath = "";   // postings.bin

    public DiskIndexWriter(Index positionalDiskIndex, String diskPath){
        this.positionalDiskIndex = positionalDiskIndex;
        this.diskPath = diskPath;
    }

    public void writeIndex(){
        try{
            RandomAccessFile raf = new RandomAccessFile(diskPath, "rw");
            raf.seek(0);
            //FileOutputStream fs = new FileOutputStream(diskPath);
            List<String> vocab = positionalDiskIndex.getVocabulary();
            for(String term : vocab){
                
                List<Posting> postings = positionalDiskIndex.getPostings(term);
                byte[] docfreqterm = ByteBuffer.allocate(4).putInt(postings.size()).array();
                raf.write(docfreqterm, 0, docfreqterm.length);

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
                    
                    for(int pos : positions){
                        byte[] posBytes = ByteBuffer.allocate(4).putInt(pos).array(); 
						raf.write(posBytes, 0, posBytes.length);
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
