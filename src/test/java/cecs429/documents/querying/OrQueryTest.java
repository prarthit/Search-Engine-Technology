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
import cecs429.querying.OrQuery;
import cecs429.querying.QueryComponent;
import cecs429.querying.TermLiteral;
import edu.csulb.TermDocumentIndexer;

public class OrQueryTest{
    private static String newDirectoryPath = "src/test/java/test_docs", fileExtension = ".json"; // Directory name where the corpus resides
    private Index index = null;
    private DocumentCorpus corpus = null;
    
    public OrQueryTest() throws IOException{
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(new File(newDirectoryPath).getAbsolutePath()), fileExtension);
        index = TermDocumentIndexer.indexCorpus(corpus);
    }

    @Test
    public void validateORQueryPostings(){
        PositionalInvertedIndexTest pr = new PositionalInvertedIndexTest();       
        String str1 = "operation+beam+chart";
        String [] s1 = str1.split("\\+");
        List<QueryComponent> component1 = new ArrayList<>();
        for(String s : s1){
            component1.add(new TermLiteral(s));
        }
        
        OrQuery or1 = new OrQuery(component1);

        String str2 = "playing+eating";
        String [] s2 = str2.split("\\+");
        List<QueryComponent> component2 = new ArrayList<>();
        for(String s : s2){
            component2.add(new TermLiteral(s));
        }
        
        OrQuery or2 = new OrQuery(component2);

        String str3 = "scan+beam";
        String [] s3 = str3.split("\\+");
        List<QueryComponent> component3 = new ArrayList<>();
        for(String s : s3){
            component3.add(new TermLiteral(s));
        }
        
        OrQuery or3 = new OrQuery(component3);

        String str4 = "abolish+theft";
        String [] s4 = str4.split("\\+");
        List<QueryComponent> component4 = new ArrayList<>();
        for(String s : s4){
            component4.add(new TermLiteral(s));
        }

        OrQuery or4 = new OrQuery(component4);

        for(Posting p : or4.getPostings(index)){
            System.out.println(p.getDocumentId());
            System.out.println(p.getPositions());
        }

        List<Posting> expected1 = new ArrayList<>();
        List<Posting> expected2 = new ArrayList<>();
        List<Posting> expected3 = new ArrayList<>();
        List<Posting> expected4 = new ArrayList<>();
        
        expected1.add(new Posting(0, new ArrayList<>(List.of(3,12,14,18,27,30,44,45,47,49))));
        expected1.add(new Posting(1, new ArrayList<>(List.of(1,3,6))));
        expected1.add(new Posting(2, new ArrayList<>(List.of(6,7,8,15))));
        expected1.add(new Posting(3, new ArrayList<>(List.of(1,4,7,16,18))));
        expected1.add(new Posting(4, new ArrayList<>(List.of(8))));

        expected2.add(new Posting(1, new ArrayList<>(List.of(19,20))));
        expected2.add(new Posting(2, new ArrayList<>(List.of(13,14))));
        expected2.add(new Posting(3, new ArrayList<>(List.of(12,13))));
        expected2.add(new Posting(4, new ArrayList<>(List.of(6,7,9,11,18,19,20,21))));

        expected3.add(new Posting(0, new ArrayList<>(List.of(11,14,26,43,47,49))));
        expected3.add(new Posting(1, new ArrayList<>(List.of(9,12))));
        expected3.add(new Posting(2, new ArrayList<>(List.of(7,15))));
        expected3.add(new Posting(3, new ArrayList<>(List.of(7))));
        expected3.add(new Posting(4, new ArrayList<>(List.of(5,8,13,15))));
        
        expected4.add(new Posting(0, new ArrayList<>(List.of(4,31,32))));
        expected4.add(new Posting(1, new ArrayList<>(List.of(18))));
        expected4.add(new Posting(2, new ArrayList<>(List.of(2,11,12,16))));
        expected4.add(new Posting(4, new ArrayList<>(List.of(1,2,16))));

        assertEquals(true, pr.checkPostings(expected1, or1.getPostings(index)));
        assertEquals(true, pr.checkPostings(expected2, or2.getPostings(index)));
        assertEquals(true, pr.checkPostings(expected3, or3.getPostings(index)));
        assertEquals(true, pr.checkPostings(expected4, or4.getPostings(index)));
    }
}