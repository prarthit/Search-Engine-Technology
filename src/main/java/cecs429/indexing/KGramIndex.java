package cecs429.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Constructs a k-gram index containing k-grams and
// words which include those k-grams
public class KGramIndex {
    private HashMap<String, List<String>> dict;
    final private int k = 3;

    public KGramIndex(List<String> words) {
        dict = new HashMap<>();
        for (String word : words) {
            addTerm(word);
        }
    }

    public int getK() {
        return k;
    }

    // Add a term to the k-gram index
    private void addTerm(String word) {
        // Generate all 1-grams, 2-grams and 3-grams for the word
        List<String> kGrams = generateKGrams(word);

        // Add word to the kGram words list in dictionary
        for (String kGram : kGrams) {
            ArrayList<String> wordsContainingKGram = (ArrayList<String>) dict.computeIfAbsent(kGram,
                    k -> new ArrayList<>());

            wordsContainingKGram.add(word);
        }
    }

    // Generate all the possible 1-grams, 2-grams...k-grams from the given string
    private List<String> generateKGrams(String word) {
        List<String> kGrams = new ArrayList<>();

        // Append '$' at beginning and end of word
        word = '$' + word + '$';
        int len = word.length();

        for (int i = 1; i <= k; i++) {
            for (int j = 0; j < len - i + 1; j++) {
                // Add the substring of size i
                kGrams.add(word.substring(j, j + i));
            }
        }

        // Remove the single '$' from 1-grams
        kGrams.remove("$");
        return kGrams;
    }

    // Return list of words containing the k-gram term
    public List<String> getWordsContainingKGram(String kGramTerm) {
        return dict.getOrDefault(kGramTerm, new ArrayList<>());
    }

    // Return list of words containing all the k-gram terms
    public List<String> getWordsContainingAllKGrams(List<String> kGramTerms) {
        List<String> wordsContainingAllKGrams = null;
        for (String kGramTerm : kGramTerms) {
            List<String> wordsContainingSingleKGram = getWordsContainingKGram(kGramTerm);
            if (wordsContainingAllKGrams == null) {
                wordsContainingAllKGrams = wordsContainingSingleKGram;
            } else {
                wordsContainingAllKGrams = intersectLists(wordsContainingAllKGrams, wordsContainingSingleKGram);
            }
        }

        return wordsContainingAllKGrams;
    }

    // Returns common strings between two lists
    public static List<String> intersectLists(List<String> list1, List<String> list2) {
        List<String> result = new ArrayList<>();

        // Use two pointers to merge both lists
        int i = 0, j = 0;
        while (i != list1.size() && j != list2.size()) {
            String currStr1 = list1.get(i), currStr2 = list2.get(j);
            if (currStr1.equals(currStr2)) {
                result.add(currStr1);
                i++;
                j++;
            } else if (currStr1.compareTo(currStr2) < 0) {
                // If str1 is lexicographically lower than str2
                i++;
            } else {
                // If str2 is lexicographically lower than str1
                j++;
            }
        }
        return result;
    }

    // Returns all the k-gram terms
    public List<String> getKGrams() {
        return new ArrayList<>(dict.keySet());
    }
}
