package cecs429.indexing.diskIndex;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.indexing.database.TermPositionCrud;
import cecs429.indexing.database.TermPositionModel;
import utils.Utils;

public class DiskIndexWriterCompressed {
    private Index positionalDiskIndex;
    private String diskDirectoryPath;

    TermPositionCrud termPositionCrud;
    TermPositionModel termPositionModel;
    
    public DiskIndexWriterCompressed(Index positionalDiskIndex, String diskDirectoryPath) throws SQLException{
        this.positionalDiskIndex = positionalDiskIndex;
        this.diskDirectoryPath = diskDirectoryPath;
    }

    public void writeIndex() throws SQLException{
        try{
            termPositionCrud = new TermPositionCrud(Utils.getDirectoryNameFromPath(diskDirectoryPath));
            termPositionCrud.createTable();

            termPositionModel = new TermPositionModel();
            VariableByteCode vbCode = new VariableByteCode();

            RandomAccessFile raf = new RandomAccessFile(diskDirectoryPath + DiskIndexEnum.POSITIONAL_INDEX.getPostingFileName(), "rw");
            raf.seek(0);

            List<String> vocab = positionalDiskIndex.getVocabulary();
            for(String term : vocab){
                
                termPositionModel.setTerm(term);
                termPositionModel.setBytePosition(raf.getChannel().position());
                termPositionCrud.add(termPositionModel);

                List<Posting> postings = positionalDiskIndex.getPostings(term);
                byte[] docFreqBytes = vbCode.encodeNumber(postings.size());
                raf.write(docFreqBytes, 0, docFreqBytes.length);

                int lastDocId = 0;
                for(Posting p : postings){
                    int docId = p.getDocumentId();

                    // docId - lastDocId includes a gap
                    byte[] docIdBytes = vbCode.encodeNumber(docId - lastDocId);
                     
					raf.write(docIdBytes, 0, docIdBytes.length);
                    
                    List<Integer> positions = p.getPositions();
                    int termFrequency = positions.size();
                    byte[] PosFreqBytes = vbCode.encodeNumber(termFrequency);
                    raf.write(PosFreqBytes, 0, PosFreqBytes.length);
                    
                    int lastPos = 0;
                    for(int pos : positions){
                        byte[] posIdBytes = vbCode.encodeNumber(pos - lastPos);
						raf.write(posIdBytes, 0, posIdBytes.length);
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
