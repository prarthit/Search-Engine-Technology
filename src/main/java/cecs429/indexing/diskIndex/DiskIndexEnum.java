package cecs429.indexing.diskIndex;

public enum DiskIndexEnum {
    POSITIONAL_INDEX("/positionalIndex.bin", "positionalIndex"),
    POSITIONAL_INDEX_ENCODED("/positionalIndexEncoded.bin", "positionalIndexEncoded"),
    POSITIONAL_INDEX_IMPACT("/positionalIndexImpact.bin", "positionalIndexImpact"),
    BIWORD_INDEX("/biwordIndex.bin", "biwordIndex"),
    KGRAM_INDEX("/kgramIndex.bin", "kgramIndex");

    private String binIndexFileName;
    private String dbIndexFileName;

    DiskIndexEnum(String binPostingFileName, String dbPostingFileName) {
        this.binIndexFileName = binPostingFileName;
        this.dbIndexFileName = dbPostingFileName;
    }

    public String getIndexFileName() {
        return this.binIndexFileName;
    }

    public String getDbIndexFileName() {
        return this.dbIndexFileName;
    }

}
