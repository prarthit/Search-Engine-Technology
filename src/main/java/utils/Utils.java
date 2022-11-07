package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.TokenProcessor;

public class Utils {
    // Returns if the given path is valid directory or not
    public static boolean isValidDirectory(String directoryPath) {
        boolean isValidDirectory = Files.isDirectory(Paths.get(directoryPath));
        if (!isValidDirectory)
            System.out.println("Invalid directory path");
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

    public static File createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory;
    }

    public static String getChildDirectoryName(String directoryPath) {
        return Paths.get(directoryPath).getFileName().toString();
    }
}
