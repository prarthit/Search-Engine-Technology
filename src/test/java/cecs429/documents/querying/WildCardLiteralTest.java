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
import cecs429.indexing.KGramIndex;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.PositionalInvertedIndexTest;
import cecs429.indexing.Posting;
import cecs429.querying.WildcardLiteral;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;

public class WildCardLiteralTest {
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where
                                                                                                 // the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
    KGramIndex kGramIndex = null;
    private TokenProcessor tokenProcessor = new AdvancedTokenProcessor();

    public WildCardLiteralTest() throws IOException {
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()),
                fileExtension);
        index = new PositionalInvertedIndex(corpus, tokenProcessor);
        kGramIndex = new KGramIndex(corpus);
    }

    @Test
    public void validateGetPostings() {
        PositionalInvertedIndexTest pt = new PositionalInvertedIndexTest();
        WildcardLiteral wildcardLiteral1 = new WildcardLiteral("p*sh", tokenProcessor, kGramIndex);
        WildcardLiteral wildcardLiteral2 = new WildcardLiteral("*ion", tokenProcessor, kGramIndex);
        WildcardLiteral wildcardLiteral3 = new WildcardLiteral("cor*t*on", tokenProcessor, kGramIndex);
        WildcardLiteral wildcardLiteral4 = new WildcardLiteral("rea*", tokenProcessor, kGramIndex);

        List<Posting> expected1 = new ArrayList<>();
        List<Posting> expected2 = new ArrayList<>();
        List<Posting> expected3 = new ArrayList<>();
        List<Posting> expected4 = new ArrayList<>();

        expected1.add(new Posting(0, new ArrayList<>(List.of(0, 15, 19, 24))));
        expected1.add(new Posting(1, new ArrayList<>(List.of(17))));
        expected1.add(new Posting(2, new ArrayList<>(List.of(0))));
        expected1.add(new Posting(3, new ArrayList<>(List.of(0, 5, 6))));

        expected2.add(new Posting(0, new ArrayList<>(List.of(1, 3, 16, 18, 20, 25, 30))));
        expected2.add(new Posting(1, new ArrayList<>(List.of(0, 1, 2, 3, 6, 7, 10))));
        expected2.add(new Posting(3, new ArrayList<>(List.of(1, 8, 9))));
        expected2.add(new Posting(4, new ArrayList<>(List.of(0))));

        expected3.add(new Posting(0, new ArrayList<>(List.of(1, 16, 20, 25))));
        expected3.add(new Posting(1, new ArrayList<>(List.of(0, 2, 7, 10))));
        expected3.add(new Posting(3, new ArrayList<>(List.of(8, 9))));
        expected3.add(new Posting(4, new ArrayList<>(List.of(0))));

        expected4.add(new Posting(0, new ArrayList<>(List.of(10, 33, 42))));
        expected4.add(new Posting(1, new ArrayList<>(List.of(23))));
        expected4.add(new Posting(2, new ArrayList<>(List.of(5, 9, 20))));
        expected4.add(new Posting(3, new ArrayList<>(List.of(14, 15, 23))));

        assertEquals(true, pt.checkPostings(expected1, wildcardLiteral1.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected2, wildcardLiteral2.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected3, wildcardLiteral3.getPostings(index)));
        assertEquals(true, pt.checkPostings(expected4, wildcardLiteral4.getPostings(index)));
    }
}
