package cecs429.documents.querying;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndexTest;
import cecs429.indexing.Posting;
import cecs429.querying.NearLiteral;
import edu.csulb.TermDocumentIndexer;

public class NearLiteralTest {
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
	
	public NearLiteralTest()throws IOException{
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()), fileExtension);
        index = TermDocumentIndexer.indexCorpus(corpus);
	}

    @Test
    public void validateNearQueryPostings(){
        PositionalInvertedIndexTest pt = new PositionalInvertedIndexTest();
        String subQuery1 = "corruption near/2 operation";
        String splittedTerms1[] = subQuery1.split("\\s+(near/)?");
		List<String> terms1 = Arrays.asList(splittedTerms1);
        NearLiteral nr1 = new NearLiteral(terms1);

        String subQuery2 = "strike near/5 lane";
        String splittedTerms2[] = subQuery2.split("\\s+(near/)?");
		List<String> terms2 = Arrays.asList(splittedTerms2);
        NearLiteral nr2 = new NearLiteral(terms2);
        
        for(Posting p : nr2.getPostings(index)){
            System.out.println(p.getDocumentId());
            // System.out.println(p.getPositions());
        }

        List<Posting> expected1 = new ArrayList<>();

        expected1.add(new Posting(0));
        expected1.add(new Posting(1));

        assertEquals(true, pt.checkPostings(expected1, nr1.getPostings(index)));
    }
}
