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

        for(Posting p : qr1.getPostings(index)){
            System.out.println(p.getDocumentId());
            System.out.println(p.getPositions());
        }

        expected1.add(new Posting(0, new ArrayList<>(List.of(11,26,43))));
        expected1.add(new Posting(1, new ArrayList<>(List.of(9,12))));
        expected1.add(new Posting(3, new ArrayList<>()));

        expected2.add(new Posting(0));
        expected2.add(new Posting(1));
        expected2.add(new Posting(2));
        expected2.add(new Posting(3));
        expected2.add(new Posting(4));

        expected3.add(new Posting(1));
        expected3.add(new Posting(2));
        expected3.add(new Posting(3));

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