package edu.csulb;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.text.AdvancedTokenProcessor;

public class TermDocumentIndexerTest {
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where
                                                                                                 // the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
    private AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

    public TermDocumentIndexerTest() throws IOException {
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()),
                fileExtension);
        index = TermDocumentIndexer.indexCorpus(corpus, processor);
        processor = new AdvancedTokenProcessor();
    }

    // Valid Directory Test
    @Test
    void validDirectory() {
        assertEquals(true, TermDocumentIndexer.isValidDirectory(newDirectoryPath));
        assertNotEquals(true, TermDocumentIndexer.isValidDirectory("   src/"));
        assertNotEquals(true, TermDocumentIndexer.isValidDirectory("src/test_docs"));
        assertNotEquals(true, TermDocumentIndexer.isValidDirectory("src/test/test_docs"));
    }

    // Generic File Reader Test
    @Test
    void validateReadFile() throws IOException {
        final PrintStream standardOut = System.out;
        final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        String path = new File("src/test/java/test_docs/1.json").getAbsolutePath();
        TermDocumentIndexer.readFile(path);

        String actual = outputStreamCaptor.toString();
        String expected = """
                {
                	"title":"Document 1",
                	"body":"punish corruption approval operation theft lemon traffic strike lane way realism scan chart agile beam punish corruption approval operation punish corruption lemon traffic strike punish corruption scan chart agile approval operation theft theft realism lemon traffic strike strike lane way lane way realism scan chart chart agile beam agile beam",
                	"url":"Document1.url"
                }
                """;

        System.setOut(standardOut);

        assertEquals(expected, actual);
    }

    // Special Queries Test
    @Test
    void validateSpecialQueries() {
        assertEquals(true,
                TermDocumentIndexer.processSpecialQueries(":index src/test/java/test_docs", processor, index));
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":stem evening", processor, index));
        assertEquals(true,
                TermDocumentIndexer.processSpecialQueries(":stem Playing football in the evening", processor, index));
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":vocab", processor, index));
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":q", processor, index));

        // stemmer not working on word+num
        assertEquals(true, TermDocumentIndexer.processSpecialQueries(":stem process123", processor, index));
    }

    // Positional Inverted index build test
    @Test
    void validatePositionalInvertedIndex() throws IOException {
        assertNotNull(TermDocumentIndexer.indexCorpus(corpus, processor));
    }
}