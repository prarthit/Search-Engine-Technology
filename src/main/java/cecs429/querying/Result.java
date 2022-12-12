package cecs429.querying;

import cecs429.documents.Document;
import cecs429.documents.FileDocument;

public class Result {
    private Document document;
    private Double accumulator = null;

    public Result(Document document) {
        this(document, null);
    }

    public Result(Document document, Double accumulator) {
        this.document = document;
        this.accumulator = accumulator;
    }

    // Return document number from the document name
    public int getDocNum() {
        String documentName = document.getTitle();
        documentName = documentName.replaceFirst("[.][^.]+$", "");
        int documentNumber = Integer.parseInt(documentName);
        return documentNumber;
    }

    @Override
    public String toString() {
        String s = "";
        s += document.getTitle()
                + " (FileName: "
                + ((FileDocument) document).getFilePath().getFileName().toString()
                + ")";
        if (accumulator != null) {
            s += "(Accumulator: " + accumulator + ")";
        }
        return s;
    }
}
