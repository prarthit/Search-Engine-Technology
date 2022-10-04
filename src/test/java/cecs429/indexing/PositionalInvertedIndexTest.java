package cecs429.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PositionalInvertedIndexTest {
    PositionalInvertedIndex positionalInvertedIndex;

    public PositionalInvertedIndexTest(){
        positionalInvertedIndex = new PositionalInvertedIndex();
    }

    public boolean checkPostings(List<Posting> expected, List<Posting>actual){
        Boolean passCase = true;
        int i = 0;
        int j = 0;
        if(expected.size() > actual.size() || actual.size() > expected.size()){
            passCase = false;
        }else{
            while(i != expected.size() && j != actual.size()){
                if(expected.get(i).getDocumentId() == actual.get(j).getDocumentId()){
                    if(expected.get(i).getPositions().equals(actual.get(j).getPositions())){
                        i++;
                        j++;   
                    }else{
                        passCase = false;
                        break;
                    }
                }
                else{
                    passCase = false;
                    break;
                }
            }
        }
        return passCase;
    }

    //add term and check if term exists in Postings
    @Test 
    public void validateAddTerm(){
        List<Posting> expected = new ArrayList<>();
        positionalInvertedIndex.addTerm("hello", 0, 0);
        positionalInvertedIndex.addTerm("bye", 1, 1);
        positionalInvertedIndex.addTerm("play", 2, 2);
        positionalInvertedIndex.addTerm("hell", 3, 3);

        expected.add(new Posting(0, 0));
        assertEquals(true, checkPostings(expected, positionalInvertedIndex.getPostings("hello")));
        expected.clear();
        expected.add(new Posting(1,1));
        assertEquals(true, checkPostings(expected, positionalInvertedIndex.getPostings("bye")));
        expected.clear();
        expected.add(new Posting(2,2));
        assertEquals(true, checkPostings(expected, positionalInvertedIndex.getPostings("play")));
        expected.clear();
        expected.add(new Posting(3,3));
        assertEquals(true, checkPostings(expected, positionalInvertedIndex.getPostings("hell")));
    }

    //check if correct postings exist in positional Inverted Index
    @Test 
    public void validategetPostings(){
        positionalInvertedIndex.addTerm("hello", 0, 0);
        positionalInvertedIndex.addTerm("bye", 1, 1);
        positionalInvertedIndex.addTerm("play", 2, 2);
        positionalInvertedIndex.addTerm("hell", 3, 3);
        
        List<Posting> expected = new ArrayList<>();
        expected.add(new Posting(0, 0));
        assertEquals(true, checkPostings(expected, positionalInvertedIndex.getPostings("hello")));
        expected.clear();
        expected.add(new Posting(1,1));
        assertEquals(true, checkPostings(expected, positionalInvertedIndex.getPostings("bye")));
        expected.clear();
        expected.add(new Posting(2,2));
        assertEquals(true, checkPostings(expected, positionalInvertedIndex.getPostings("play")));
        expected.clear();
        expected.add(new Posting(3,3));
        assertEquals(true, checkPostings(expected, positionalInvertedIndex.getPostings("hell")));
    }

    //Check if correct vocabulary is built
    @Test
    public void validategetVocabulary(){
        positionalInvertedIndex.addTerm("hello", 0, 0);
        positionalInvertedIndex.addTerm("hell", 3, 3);
        positionalInvertedIndex.addTerm("bye", 1, 1);
        positionalInvertedIndex.addTerm("play", 2, 2);

        List<String> vocab = new ArrayList<>(List.of("hello","bye","play", "hell"));
        assertNotEquals(vocab, positionalInvertedIndex.getVocabulary());
        List<String> vocab1 = new ArrayList<>(List.of("hello","play","bye", "hell"));
        assertNotEquals(vocab1, positionalInvertedIndex.getVocabulary());
        List<String> vocab2 = new ArrayList<>(List.of("bye", "hell","hello", "play"));
        assertEquals(vocab2, positionalInvertedIndex.getVocabulary());
        List<String> vocab3 = new ArrayList<>(List.of("bye", "hell0","hell", "play"));
        assertNotEquals(vocab3, positionalInvertedIndex.getVocabulary());
    }
}
