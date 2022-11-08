package cecs429.querying.variantFormulas;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Properties;

import utils.Utils;

public class DocWeightsWriter {
    public static String getDocWeightFilePath() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("src/config.properties"));
        } catch (Exception e) {
            System.err.println("Cannot read config.properties file");
            e.printStackTrace();
        }

        String path = prop.getProperty("resources_dir") + "/" + Utils.getDirectoryNameFromPath(prop.getProperty("corpus_directory_path")) + "_docWeights.bin";
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
