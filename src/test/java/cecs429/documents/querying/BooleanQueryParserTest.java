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
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.QueryComponent;
import edu.csulb.TermDocumentIndexer;

public class BooleanQueryParserTest{
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
	
	public BooleanQueryParserTest()throws IOException{
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()), fileExtension);
        index = TermDocumentIndexer.indexCorpus(corpus);
	}

    @Test
    public void validateBooleanQueryParser(){
        BooleanQueryParser bqp = new BooleanQueryParser();
        PositionalInvertedIndexTest pt = new PositionalInvertedIndexTest();
        
        QueryComponent qr1 = bqp.parseQuery("punish operation + lemon + scan");
        QueryComponent qr2 = bqp.parseQuery("agile agile + playing playing");
        QueryComponent qr3 = bqp.parseQuery("realism eating + playing");
        QueryComponent qr4 = bqp.parseQuery("strike scan theft lemon");
        QueryComponent qr5 = bqp.parseQuery("approval abolish theft corruption");

        List<Posting> expected1 = new ArrayList<>();
        List<Posting> expected2 = new ArrayList<>();
        List<Posting> expected3 = new ArrayList<>();
        List<Posting> expected4 = new ArrayList<>();
        List<Posting> expected5 = new ArrayList<>();

        expected1.add(new Posting(0, new ArrayList<>(List.of(5,11,21,26,34,43))));
        expected1.add(new Posting(1, new ArrayList<>(List.of(4,9,12))));
        expected1.add(new Posting(3, new ArrayList<>()));
        expected1.add(new Posting(4, new ArrayList<>(List.of(5,12,13,15))));

        expected2.add(new Posting(0));
        expected2.add(new Posting(1));
        expected2.add(new Posting(2));
        expected2.add(new Posting(3));
        expected2.add(new Posting(4));

        expected3.add(new Posting(1, new ArrayList<>(List.of(19))));
        expected3.add(new Posting(2, new ArrayList<>(List.of(14))));
        expected3.add(new Posting(3, new ArrayList<>(List.of(12))));
        expected3.add(new Posting(4, new ArrayList<>(List.of(18,19,20,21))));

        expected4.add(new Posting(0));
        expected4.add(new Posting(4));

        expected5.add(new Posting(4));
        
        assertEquals(true, pt.checkPostings(expected1, qr1.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected2, qr2.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected3, qr3.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected4, qr4.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected5, qr5.getPostings(index)));
    }
}