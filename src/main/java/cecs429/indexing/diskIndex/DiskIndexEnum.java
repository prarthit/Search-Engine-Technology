package cecs429.indexing.diskIndex;

public enum DiskIndexEnum {
    POSITIONAL_INDEX("_postings.bin", "_postings"),
    BIWORD_INDEX("_biwordPostings.bin", "_biwordPostings"),
    KGRAM_INDEX("_kgramPostings.bin", "_kgramPostings");

    private String binPostingFileName;
    private String dbPostingFileName;

    DiskIndexEnum(String binPostingFileName, String dbPostingFileName){
        this.binPostingFileName = binPostingFileName;
        this.dbPostingFileName = dbPostingFileName;
    }

    public String getPostingFileName(){
        return this.binPostingFileName;
    }

    public String getDbPostingFileName(){
        return this.dbPostingFileName;
    }

}
