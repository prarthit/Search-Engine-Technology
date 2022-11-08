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
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.PositionalInvertedIndexTest;
import cecs429.indexing.Posting;
import cecs429.querying.NearLiteral;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;

public class NearLiteralTest {
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where
                                                                                                 // the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
    private TokenProcessor tokenProcessor = new AdvancedTokenProcessor();

    public NearLiteralTest() throws IOException {
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()),
                fileExtension);
        index = new PositionalInvertedIndex(corpus, tokenProcessor);
    }

    @Test
    public void validateNearQueryPostings() {
        PositionalInvertedIndexTest pt = new PositionalInvertedIndexTest();

        String subQuery1 = "corruption near/2 operation";
        String splittedTerms1[] = subQuery1.split("\\s+(near/)?");
        List<String> terms1 = Arrays.asList(splittedTerms1);
        NearLiteral nr1 = new NearLiteral(terms1, tokenProcessor, null);

        String subQuery2 = "strike near/5 lane";
        String splittedTerms2[] = subQuery2.split("\\s+(near/)?");
        List<String> terms2 = Arrays.asList(splittedTerms2);
        NearLiteral nr2 = new NearLiteral(terms2, tokenProcessor, null);

        String subQuery3 = "playing near/2 eating";
        String splittedTerms3[] = subQuery3.split("\\s+(near/)?");
        List<String> terms3 = Arrays.asList(splittedTerms3);
        NearLiteral nr3 = new NearLiteral(terms3, tokenProcessor, null);

        String subQuery4 = "punish near/5 abolish";
        String splittedTerms4[] = subQuery4.split("\\s+(near/)?");
        List<String> terms4 = Arrays.asList(splittedTerms4);
        NearLiteral nr4 = new NearLiteral(terms4, tokenProcessor, null);

        String subQuery5 = "traffic near/9 lane";
        String splittedTerms5[] = subQuery5.split("\\s+(near/)?");
        List<String> terms5 = Arrays.asList(splittedTerms5);
        NearLiteral nr5 = new NearLiteral(terms5, tokenProcessor, null);

        for (Posting p : nr5.getPostings(index)) {
            System.out.println(p.getDocumentId());
            System.out.println(p.getPositions());
        }

        List<Posting> expected1 = new ArrayList<>();
        List<Posting> expected2 = new ArrayList<>();
        List<Posting> expected3 = new ArrayList<>();
        List<Posting> expected4 = new ArrayList<>();
        List<Posting> expected5 = new ArrayList<>();

        expected1.add(new Posting(0, new ArrayList<>(List.of(3, 18))));
        expected1.add(new Posting(1, new ArrayList<>(List.of(1, 3))));

        expected2.add(new Posting(0, new ArrayList<>(List.of(8, 38, 40))));

        expected3.add(new Posting(1, new ArrayList<>(List.of(20))));
        expected3.add(new Posting(3, new ArrayList<>(List.of(13))));

        expected4.add(new Posting(1, new ArrayList<>(List.of(18))));

        expected5.add(new Posting(0, new ArrayList<>(List.of(8, 38, 40))));
        expected5.add(new Posting(1, new ArrayList<>(List.of(21))));
        expected5.add(new Posting(2, new ArrayList<>(List.of(4, 10, 18))));
        expected5.add(new Posting(3, new ArrayList<>(List.of(10, 11, 21))));

        assertEquals(true, pt.checkPostings(expected1, nr1.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected2, nr2.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected3, nr3.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected4, nr4.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected5, nr5.getPostings(index)));
    }
}
