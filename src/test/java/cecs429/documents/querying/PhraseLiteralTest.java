package cecs429.documents.querying;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndexTest;
import cecs429.indexing.Posting;
import cecs429.querying.PhraseLiteral;
import edu.csulb.TermDocumentIndexer;

public class PhraseLiteralTest{
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
    
    public PhraseLiteralTest() throws IOException{
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()), fileExtension);
        index = TermDocumentIndexer.indexCorpus(corpus);
    }

    @Test
    void validategetPostings(){
        PositionalInvertedIndexTest pt = new PositionalInvertedIndexTest();
        PhraseLiteral pl1 = new PhraseLiteral("scan chart");
        PhraseLiteral pl2 = new PhraseLiteral("lane way realism");
        PhraseLiteral pl3 = new PhraseLiteral("strike strike");
        PhraseLiteral pl4 = new PhraseLiteral("playing playing playing playing");
        
        List<Posting> expected1 = new ArrayList<>();
        List<Posting> expected2 = new ArrayList<>();
        List<Posting> expected3 = new ArrayList<>();
        List<Posting> expected4 = new ArrayList<>();

        expected1.add(new Posting(0, new ArrayList<>(List.of(12,27,44))));
        expected2.add(new Posting(0, new ArrayList<>(List.of(10,42))));
        expected2.add(new Posting(1, new ArrayList<>(List.of(23))));
        expected2.add(new Posting(2, new ArrayList<>(List.of(20)))); 
        expected2.add(new Posting(3, new ArrayList<>(List.of(23))));
        expected3.add(new Posting(0, new ArrayList<>(List.of(37))));
        expected3.add(new Posting(4, new ArrayList<>(List.of(4))));
        expected4.add(new Posting(4, new ArrayList<>(List.of(21))));

        assertEquals(true, pt.checkPostings(expected1, pl1.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected2, pl2.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected3, pl3.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected4, pl4.getPostings(index)));
    }
}