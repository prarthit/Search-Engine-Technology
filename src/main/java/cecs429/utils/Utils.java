package cecs429.utils;

import java.io.File;
import java.nio.file.Paths;

public class Utils {
    public static File createDirectory(String path){
		File directory = new File(path);
		if (!directory.exists()){
			directory.mkdirs();
		}

		return directory;
	}

	public static String getChildDirectoryName(String directoryPath){
		return Paths.get(directoryPath).getFileName().toString();
	}
}
