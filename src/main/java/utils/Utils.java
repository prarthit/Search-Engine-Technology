package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Properties;

import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.TokenProcessor;

public class Utils {
    private static String corpusName = "searchEngine";

    // Returns if the given path is valid directory or not
    public static boolean isValidDirectory(String directoryPath) {
        boolean isValidDirectory = Files.isDirectory(Paths.get(directoryPath));
        return isValidDirectory;
    }

    // Generic file reader
    public static void readFile(String filepath) throws IOException {
        BufferedReader in;
        in = new BufferedReader(new FileReader(filepath));
        String line = in.readLine();
        while (line != null) {
            System.out.println(line);
            line = in.readLine();
        }
        in.close();
    }

    // Returns appropriate token processor
    public static TokenProcessor getTokenProcessor(String tokenProcessor) {
        if (tokenProcessor.equals("BASIC")) {
            return new BasicTokenProcessor();
        } else {
            return new AdvancedTokenProcessor();
        }
    }

    // Check if directory exists at path, if not create directory
    public static File createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory;
    }

    // Returns a file path prefix based on corpus name
    // for disk index files
    public static String generateFilePathPrefix() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("src/config.properties"));
        } catch (Exception e) {
            System.err.println("Cannot read config.properties file");
            e.printStackTrace();
        }

        String filePathPrefix = prop.getProperty("resources_dir") + "/" + corpusName;
        return filePathPrefix;
    }

    public static void setCorpusName(String name) {
        corpusName = name;
        createDirectory(generateFilePathPrefix()); // Create directory for corpus if does not exist
    }

    public static Properties getProperties() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("src/config.properties"));
        } catch (IOException e) {
            System.err.println("Unable to read properties file");
            e.printStackTrace();
        }

        return prop;
    }

    public static double formatDouble(double num) {
        DecimalFormat df = new DecimalFormat("#.##");
        Double formattedNum = Double.parseDouble(df.format(num));

        return formattedNum;
    }
}
