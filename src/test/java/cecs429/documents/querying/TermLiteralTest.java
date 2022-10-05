package cecs429.documents.querying;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndexTest;
import cecs429.indexing.Posting;
import cecs429.querying.TermLiteral;
import edu.csulb.TermDocumentIndexer;

public class TermLiteralTest{
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
	
	public TermLiteralTest()throws IOException{
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()), fileExtension);
        index = TermDocumentIndexer.indexCorpus(corpus);
	}
    
    @Test
    public void validateLiterals(){
        PositionalInvertedIndexTest pt = new PositionalInvertedIndexTest();
        TermLiteral tl1 = new TermLiteral("punish");
        TermLiteral tl2 = new TermLiteral("lemon");
        TermLiteral tl3 = new TermLiteral("realism");
        TermLiteral tl4 = new TermLiteral("corruption");
        TermLiteral tl5 = new TermLiteral("operation");

        List<Posting> punishPosting = new ArrayList<>();
        List<Posting> lemonPosting = new ArrayList<>();
        List<Posting> realismPosting = new ArrayList<>();
        List<Posting> corruptionPosting = new ArrayList<>();
        List<Posting> operationPosting = new ArrayList<>();

        punishPosting.add(new Posting(0, new ArrayList<>(List.of(0,15,19,24))));
        punishPosting.add(new Posting(1, new ArrayList<>(List.of(17))));
        punishPosting.add(new Posting(2, new ArrayList<>(List.of(0))));
        punishPosting.add(new Posting(3, new ArrayList<>(List.of(0,5,6))));

        lemonPosting.add(new Posting(0, new ArrayList<>(List.of(5,21,34))));
        lemonPosting.add(new Posting(1, new ArrayList<>(List.of(4))));
        lemonPosting.add(new Posting(4, new ArrayList<>(List.of(12))));

        realismPosting.add(new Posting(0, new ArrayList<>(List.of(10,33,42))));
        realismPosting.add(new Posting(1, new ArrayList<>(List.of(23))));
        realismPosting.add(new Posting(2, new ArrayList<>(List.of(5,9, 20))));
        realismPosting.add(new Posting(3, new ArrayList<>(List.of(14,15, 23))));

        corruptionPosting.add(new Posting(0, new ArrayList<>(List.of(1,16,20, 25))));
        corruptionPosting.add(new Posting(1, new ArrayList<>(List.of(0,2,7, 10))));
        corruptionPosting.add(new Posting(3, new ArrayList<>(List.of(8,9))));
        corruptionPosting.add(new Posting(4, new ArrayList<>(List.of(0))));

        operationPosting.add(new Posting(0, new ArrayList<>(List.of(3,18,30))));
        operationPosting.add(new Posting(1, new ArrayList<>(List.of(1,3,6))));
        operationPosting.add(new Posting(3, new ArrayList<>(List.of(1))));

        assertEquals(true, pt.checkPostings(punishPosting, tl1.getPostings(index)));
        assertEquals(true, pt.checkPostings(lemonPosting, tl2.getPostings(index)));
        assertEquals(true, pt.checkPostings(realismPosting, tl3.getPostings(index)));
        assertEquals(true, pt.checkPostings(corruptionPosting, tl4.getPostings(index)));
        assertEquals(true, pt.checkPostings(operationPosting, tl5.getPostings(index)));
    }
}