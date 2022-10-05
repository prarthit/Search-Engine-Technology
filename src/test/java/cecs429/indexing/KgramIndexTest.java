package cecs429.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cecs429.text.AdvancedTokenProcessor;

public class KgramIndexTest {
    KGramIndex kgramIndex;
    public KgramIndexTest(){
        List<String> words = new ArrayList<>(List.of("pollution","nation", "corruption", "strike", "like"));
        AdvancedTokenProcessor advancedTokenProcessor = new AdvancedTokenProcessor();
        List<String> processedWords = new ArrayList<>();
        for(String s : words){
            processedWords.add(advancedTokenProcessor.preProcessToken(s));
        }
        kgramIndex = new KGramIndex(processedWords);
    }

    @Test
    public void validateGetWordsContainingKGram(){
        List<String> threeLetterGram = new ArrayList<>(List.of("pollution", "nation", "corruption"));
        List<String> twoLetterGram = new ArrayList<>(List.of("strike","like"));
        List<String> oneLetterGram = new ArrayList<>(List.of("pollution", "nation", "corruption", "strike"));
        
        assertEquals(threeLetterGram, kgramIndex.getWordsContainingKGram("ion"));
        assertEquals(twoLetterGram, kgramIndex.getWordsContainingKGram("ik"));
        assertEquals(oneLetterGram, kgramIndex.getWordsContainingKGram("t"));
    }

    @Test
    public void validateGetWordsContainingAllKGrams(){
        List<String> gramTerms1 = new ArrayList<>(List.of("t", "ik", "i"));
        List<String> expectedgramTerms1 = new ArrayList<>(List.of("strike"));
        
        List<String> gramTerms2 = new ArrayList<>(List.of("on", "t", "i"));
        List<String> expectedgramTerms2 = new ArrayList<>(List.of("pollution","nation","corruption"));

        List<String> gramTerms3 = new ArrayList<>(List.of( "i"));
        List<String> expectedgramTerms3 = new ArrayList<>(List.of("pollution","nation","corruption", "strike", "like"));

        List<String> gramTerms4 = new ArrayList<>(List.of( "ut", "upt", "k"));
        List<String> expectedgramTerms4 = new ArrayList<>();

        assertEquals(expectedgramTerms1, kgramIndex.getWordsContainingAllKGrams(gramTerms1));
        assertEquals(expectedgramTerms2, kgramIndex.getWordsContainingAllKGrams(gramTerms2));
        assertEquals(expectedgramTerms3, kgramIndex.getWordsContainingAllKGrams(gramTerms3));
        assertEquals(expectedgramTerms4, kgramIndex.getWordsContainingAllKGrams(gramTerms4));
    }

    @Test
    public void validateintersectLists(){

        List<String> expected1 = new ArrayList<>(List.of("punish"));
        List<String> intersect1 = new ArrayList<>(List.of("punish", "strike", "abolish"));
        List<String> intersect2 = new ArrayList<>(List.of("punish", "hello", "lello"));
        
        List<String> expected2 = new ArrayList<>(List.of("punish", "strike"));
        List<String> intersect3 = new ArrayList<>(List.of("punish", "strike", "abolish"));
        List<String> intersect4 = new ArrayList<>(List.of("punish", "hello", "lello", "strike"));

        List<String> expected3 = new ArrayList<>();
        List<String> intersect5 = new ArrayList<>(List.of("punish", "strike", "abolish"));
        List<String> intersect6 = new ArrayList<>(List.of("othello", "hello", "lello", "hell"));

        List<String> expected4 = new ArrayList<>();
        List<String> intersect7 = new ArrayList<>(List.of());
        List<String> intersect8 = new ArrayList<>(List.of());

        assertEquals(expected1, kgramIndex.intersectLists(intersect1, intersect2));
        assertEquals(expected2, kgramIndex.intersectLists(intersect3, intersect4));
        assertEquals(expected3, kgramIndex.intersectLists(intersect5, intersect6));
        assertEquals(expected4, kgramIndex.intersectLists(intersect7, intersect8));
    }
}
