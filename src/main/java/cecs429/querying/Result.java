package cecs429.querying;

import cecs429.documents.Document;

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
        String documentName = document.getDocumentName();
        documentName = documentName.replaceFirst("[.][^.]+$", "");
        int documentNumber = Integer.parseInt(documentName);
        return documentNumber;
    }

    @Override
    public String toString() {
        String s = "";
        s += document.getTitle()
                + " (FileName: "
                + document.getDocumentName()
                + ")";
        if (accumulator != null) {
            s += "(Accumulator: " + accumulator + ")";
        }
        return s;
    }
}
