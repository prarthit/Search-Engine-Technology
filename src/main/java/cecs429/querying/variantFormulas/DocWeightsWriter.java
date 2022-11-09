package cecs429.querying.variantFormulas;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import utils.Utils;

public class DocWeightsWriter {
    public static String getDocWeightFilePath() {
        String path = Utils.generateFilePathPrefix() + "/docWeights.bin";
        return path;
    }

    // Write doc weights to disk
    public static void writeToDisk(List<DocWeights> docWeightsList) {
        String path = getDocWeightFilePath();

        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            long sum_of_docLengths = 0;
            for (DocWeights docWeights : docWeightsList) {
                raf.writeDouble(docWeights.getDocWeight());
                raf.writeInt(docWeights.getDocLength());
                raf.writeLong(docWeights.getByteSize());
                raf.writeDouble(docWeights.getAvg_tf_td());

                sum_of_docLengths += docWeights.getDocLength();
            }

            double avgDocLength = sum_of_docLengths / (double) docWeightsList.size();
            raf.writeDouble(avgDocLength);
        } catch (IOException e) {
            System.err.println("Cannot write doc weights to disk");
            e.printStackTrace();
        }
    }
}
