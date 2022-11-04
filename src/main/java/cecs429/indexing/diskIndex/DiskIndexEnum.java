package cecs429.indexing.diskIndex;

public enum DiskIndexEnum {
    POSITIONAL_INDEX("_postings.bin"),
    BIWORD_INDEX("_biwordPostings.bin"),
    KGRAM_INDEX("_kgramPostings.bin");

    private String binPostingFileName;

    DiskIndexEnum(String binPostingFileName){
        this.binPostingFileName = binPostingFileName;
    }

    public String getPostingFileName(){
        return this.binPostingFileName;
    }

}
