package cecs429.querying.variantFormulas;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DocWeightsReader {
    public static DocWeights readDocWeights(int documentId, RandomAccessFile raf) {
        try {
            raf.seek(documentId * DocWeights.getClassByteSize());

            double docWeight = raf.readDouble();
            int docLength = raf.readInt();
            long byteSize = raf.readLong();
            double ave_tf_td = raf.readDouble();

            return new DocWeights(docWeight, docLength, byteSize, ave_tf_td);
        } catch (IOException e) {
            System.err.println("Cannot read document weights for document id: " + documentId);
            e.printStackTrace();
        }

        return null;
    }

    public static double readAvgDocLength(RandomAccessFile raf) {
        try {
            raf.seek(raf.length() - 8);
            double avgDocLength = raf.readDouble();

            return avgDocLength;
        } catch (IOException e) {
            System.err.println("Cannot read average document length");
            e.printStackTrace();
        }

        return 0.0;
    }
}
