package cecs429.documents.querying;

import static org.junit.Assert.assertEquals;

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
import cecs429.querying.AndQuery;
import cecs429.querying.QueryComponent;
import cecs429.querying.TermLiteral;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;
import edu.csulb.TermDocumentIndexer;

public class AndQueryTest {
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where
                                                                                                 // the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
    private TokenProcessor tokenProcessor = new AdvancedTokenProcessor();

    public AndQueryTest() throws IOException {
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()),
                fileExtension);
        index = TermDocumentIndexer.indexCorpus(corpus, tokenProcessor);
    }

    @Test
    public void validateANDQueryPostings() {
        PositionalInvertedIndexTest pr = new PositionalInvertedIndexTest();
        String str1 = "operation beam chart";
        String[] s1 = str1.split(" ");
        List<QueryComponent> component1 = new ArrayList<>();
        for (String s : s1) {
            component1.add(new TermLiteral(s, tokenProcessor));
        }

        AndQuery and1 = new AndQuery(component1);

        String str2 = "abolish theft";
        String[] s2 = str2.split(" ");
        List<QueryComponent> component2 = new ArrayList<>();
        for (String s : s2) {
            component2.add(new TermLiteral(s, tokenProcessor));
        }

        AndQuery and2 = new AndQuery(component2);

        String str3 = "traffic lane approval";
        String[] s3 = str3.split(" ");
        List<QueryComponent> component3 = new ArrayList<>();
        for (String s : s3) {
            component3.add(new TermLiteral(s, tokenProcessor));
        }

        AndQuery and3 = new AndQuery(component3);

        String str4 = "punish agile lemon";
        String[] s4 = str4.split(" ");
        List<QueryComponent> component4 = new ArrayList<>();
        for (String s : s4) {
            component4.add(new TermLiteral(s, tokenProcessor));
        }

        AndQuery and4 = new AndQuery(component4);

        List<Posting> expected1 = new ArrayList<>();
        List<Posting> expected2 = new ArrayList<>();
        List<Posting> expected3 = new ArrayList<>();
        List<Posting> expected4 = new ArrayList<>();

        expected1.add(new Posting(0));
        expected1.add(new Posting(3));

        expected2.add(new Posting(2));
        expected2.add(new Posting(4));

        expected3.add(new Posting(0));
        expected3.add(new Posting(1));
        expected3.add(new Posting(2));

        expected4.add(new Posting(0));
        expected4.add(new Posting(1));

        assertEquals(true, pr.checkPostings(expected1, and1.getPostings(index)));
        assertEquals(true, pr.checkPostings(expected2, and2.getPostings(index)));
        assertEquals(true, pr.checkPostings(expected3, and3.getPostings(index)));
        assertEquals(true, pr.checkPostings(expected4, and4.getPostings(index)));
    }
}