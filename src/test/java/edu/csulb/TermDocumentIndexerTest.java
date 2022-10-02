package edu.csulb;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.text.AdvancedTokenProcessor;

public class TermDocumentIndexerTest{
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
    private AdvancedTokenProcessor processor = null;
    
    public TermDocumentIndexerTest() throws IOException{
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()), fileExtension);
        index = TermDocumentIndexer.indexCorpus(corpus);
        processor = new AdvancedTokenProcessor();
    }
    
    //Valid Directory Test
    @Test
    void validDirectory(){
        assertEquals(true, TermDocumentIndexer.isValidDirectory(newDirectoryPath));
        assertNotEquals(true, TermDocumentIndexer.isValidDirectory("   src/"));
        assertNotEquals(true, TermDocumentIndexer.isValidDirectory("src/test_docs"));
        assertNotEquals(true, TermDocumentIndexer.isValidDirectory("src/test/test_docs"));
    }

    //Generic File Reader Test
    @Test
    void validateReadFile() throws IOException{

        for(int i=1; i<=5 ; i++){
            assertNotNull(TermDocumentIndexer.readFile(newDirectoryPath + "/" + i + fileExtension));
        }
    }

    //Special Queries Test
    @Test
    void validateSpecialQueries(){
        assertEquals(true ,TermDocumentIndexer.processSpecialQueries(":index src/test/java/test_docs", processor, index));   
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":stem evening", processor, index));    
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":stem Playing football in the evening", processor, index));    
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":vocab", processor, index));
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":q", processor, index));
        
        //stemmer not working on word+num
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":stem process123", processor, index));
    }

    //Positional Inverted index build test
    @Test
    void validatePositionalInvertedIndex()throws IOException{
        assertNotNull(TermDocumentIndexer.indexCorpus(corpus));
    }

}


/*
 * public PositionalInvertedIndexTest() throws IOException{
        
        processor = new AdvancedTokenProcessor();
    }

    private static List<List<Integer>> findQuery(String query, Index index, DocumentCorpus corpus) {
		int queryFoundInFilesCount = 0;
		List <List<Integer>> DocumentList = new ArrayList<>();
		List <Integer> positionsList = new ArrayList<>(); 

		BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
		QueryComponent queryComponent = booleanQueryParser.parseQuery(query);

		if (queryComponent != null) {
			for (Posting p : queryComponent.getPostings(index)) {
				queryFoundInFilesCount++;
				Document queryFoundInDocument = corpus.getDocument(p.getDocumentId());
				positionsList.add(p.getDocumentId() + 1);
				System.out.println(queryFoundInDocument.getTitle()
						+ " (FileName: "
						+ ((FileDocument) queryFoundInDocument).getFilePath().getFileName().toString()
						+ ")");

				p.getPositions().forEach(Position -> positionsList.add(Position + 1));
			}
        }
        DocumentList.add(positionsList);
        System.out.println(queryFoundInFilesCount);
		System.out.println(DocumentList);
        return DocumentList;
    }

    // Single term test
    @Test   
    void validateTermLiterals(){
        List<List<Integer>> punishlists = List.of(List.of(1, 1, 16, 20, 25, 2, 18, 3, 1, 4, 1, 6, 7));
        List<List<Integer>> abolishList = List.of(List.of(2, 19, 3, 17, 5, 17));
        List<List<Integer>> strikeList = List.of(List.of(1, 8, 24, 37, 38, 2, 6, 14, 5, 4, 5));
        assertEquals(punishlists, findQuery("punish", index, corpus));
        assertEquals(abolishList, findQuery("abolish", index, corpus));
        assertEquals(strikeList, findQuery("strike", index, corpus));
    }

    // Phrase literal test
    @Test
    void validatePhraseLiterals(){
        List<List<Integer>> laneAndWay = List.of(List.of(1, 4)); //1, 3, 4
        List<List<Integer>> beamAndPunish = List.of(List.of(1, 3, 4));
        List<List<Integer>> beamORPunish = List.of(List.of(1, 2, 3, 4));
        assertEquals(laneAndWay, findQuery("lane way", index, corpus));
        assertEquals(beamAndPunish, findQuery("beam punish", index, corpus));
        assertEquals(beamORPunish, findQuery("beam + punish", index, corpus));
    }
 */