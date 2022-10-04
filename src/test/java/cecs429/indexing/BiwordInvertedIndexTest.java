package cecs429.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BiwordInvertedIndexTest {
    BiwordInvertedIndex bi;
    PositionalInvertedIndexTest pt;
    
    public BiwordInvertedIndexTest(){
        bi = new BiwordInvertedIndex();
        pt = new PositionalInvertedIndexTest();
    }

    @Test
    public void validateAddTerm(){
        
        bi.addTerm("strike strike", 0);
        bi.addTerm("punish punish", 3);
        bi.addTerm("punish punish", 4);
        bi.addTerm("corruption corruption", 5);
        
        List<Posting> expected1 = new ArrayList<>();
        List<Posting> expected2 = new ArrayList<>();
        List<Posting> expected3 = new ArrayList<>();
        List<Posting> nullposting = new ArrayList<>();
    
        expected1.add(new Posting(0));
        expected2.add(new Posting(3));
        expected2.add(new Posting(4));
        expected3.add(new Posting(5));
        
        assertEquals(true, pt.checkPostings(expected1, bi.getPostings("strike strike")));
        assertEquals(true, pt.checkPostings(expected2, bi.getPostings("punish punish")));
        assertEquals(true, pt.checkPostings(expected3, bi.getPostings("corruption corruption")));
        assertEquals(nullposting, bi.getPostings("play play"));
    }

    @Test
    public void validategetVocabulary(){
        List<String> expectedvocab = new ArrayList<>(List.of("hello othello", "punish punish", "scan scan", "scan theft"));
        List<String> emptyVocab = new ArrayList<>();
        BiwordInvertedIndex emptybi = new BiwordInvertedIndex();
        bi.addTerm("punish punish", 0);
        bi.addTerm("scan theft", 1);
        bi.addTerm("scan scan", 2);
        bi.addTerm("hello othello", 3);
        assertEquals(expectedvocab, bi.getVocabulary());
        assertEquals(emptyVocab, emptybi.getVocabulary());
    }
}
