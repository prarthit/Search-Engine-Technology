package edu.csulb;

import cecs429.indexing.Index;
import cecs429.indexing.KGramIndex;
import cecs429.text.TokenProcessor;
import utils.Utils;

public class EngineStore {
    private static Index index;
    private static KGramIndex kGramIndex;
    private static Index biwordIndex;
    private static TokenProcessor tokenProcessor;

    public static Index getIndex() {
        return index;
    }

    public static void setIndex(Index index) {
        EngineStore.index = index;
    }

    public static KGramIndex getkGramIndex() {
        return kGramIndex;
    }

    public static void setkGramIndex(KGramIndex kGramIndex) {
        EngineStore.kGramIndex = kGramIndex;
    }

    public static Index getBiwordIndex() {
        return biwordIndex;
    }

    public static void setBiwordIndex(Index biwordIndex) {
        EngineStore.biwordIndex = biwordIndex;
    }

    public static TokenProcessor getTokenProcessor() {
        if (tokenProcessor == null) {
            // Create basic or advanced token processor based on properties file
            TokenProcessor processor = Utils
                    .getTokenProcessor(Utils.getProperties().getProperty("token_processor"));
            tokenProcessor = processor;
        }

        return tokenProcessor;
    }
}
