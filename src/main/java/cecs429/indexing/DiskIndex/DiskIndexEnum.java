package cecs429.indexing.DiskIndex;

public enum DiskIndexEnum {
    POSITIONAL_INDEX("", ""),
    BIWORD_INDEX( "",""),
    KGRAM_INDEX("", "");

    private String binVocabFileName;
    private String binPostingFileName;

    DiskIndexEnum(String binVocabFileName, String binPostingFileName){
        this.binVocabFileName = binVocabFileName;
        this.binPostingFileName = binPostingFileName;
    }

    public String getVocabFileName(){
        return this.binVocabFileName;
    }

    public String getPostingFileName(){
        return this.binPostingFileName;
    }

}
