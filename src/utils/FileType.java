package utils;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;

public class FileType {
	
	public String getFileType(File file) {
		
		Tika tika = new Tika();
		String fileType = null;

	    try {
			fileType = tika.detect(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return fileType;
	}
}