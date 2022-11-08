package cecs429.indexing.diskIndex;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DocWeightsWriter {
    public static double calculateDocWeight(Map<String, Integer> termFreqMap) {
        // Sum of term weights
        double sum_of_w_dt = 0;

        for (int freq : termFreqMap.values()) {
            double w_dt = 1 + Math.log10(freq); // Weight of a term in a document
            sum_of_w_dt = w_dt * w_dt;
        }

        // Weight of document
        double L_d = Math.sqrt(sum_of_w_dt); // Normalize sum of term weights

        return L_d;
    }

    public static String getDocWeightFilePath() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("src/config.properties"));
        } catch (Exception e) {
            System.err.println("Cannot read config.properties file");
            e.printStackTrace();
        }

        String path = prop.getProperty("resources_dir") + "/docWeights.bin";
        return path;
    }

    // Write doc weights to disk
    public static void writeToDisk(List<Double> docWeights) {
        String path = getDocWeightFilePath();
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            for (Double docWeight : docWeights) {
                raf.writeDouble(docWeight);
            }
        } catch (IOException e) {
            System.err.println("Cannot write doc weights to disk");
            e.printStackTrace();
        }
    }
}
