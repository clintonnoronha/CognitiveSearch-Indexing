package impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import utils.FileType;
import utils.LuceneIndexUtils;

public class MainDriver {
	
	private static FileType obj = new FileType();

	public static void main(String[] args) {
		
		LuceneIndexUtils utils = new LuceneIndexUtils("indexLocation");
		
		try {
			Files.walk(Path.of("C:\\Users\\Clinton\\Desktop\\TestFiles"))
	    	.forEach(filePath -> {
	    		File file = new File(filePath.toString());
	    		if (file.isFile()) {
	    			
	    			String fileType = obj.signature(filePath.toString());
					
					switch (fileType.toLowerCase()) {
						case "pdf":
							// PDF Files Handling
							try {
								utils.indexPDFDocument(file, utils.getIdxWriter(), utils.getIdxWriterConfig());
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case "txt":
							// TXT Files Handling
							
							break;
						case "doc":
							// DOC Files Handling
							
							break;
						case "ppt":
							// PPT Files Handling
							
							break;
						default:
							// Other Files which aren't processable
							break;	
					}
	    		}
	    	});
			
			utils.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}