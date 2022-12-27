package impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import utils.FileType;
import utils.LuceneIndexUtils;

public class MainDriver {

	public static void main(String[] args) {
		
		LuceneIndexUtils utils = new LuceneIndexUtils("indexLocation");
		
		FileType obj = new FileType();
		
		long start = System.currentTimeMillis();
		
		try {
			Files.walk(Path.of("C:\\Users\\Clinton\\Desktop\\TestFiles"))
	    	.forEach(filePath -> {
	    		File file = new File(filePath.toString());
	    		if (file.isFile()) {
	    			
	    			String fileType = obj.getFileType(file);
					
	    			try {
	    				if (utils.isCreateNew()) {
							utils.indexDocument(file, utils.getIdxWriter(), utils.getIdxWriterConfig(), fileType);
							utils.getIdxWriterConfig().setOpenMode(OpenMode.APPEND);
						} else {
							utils.indexDocument(file, utils.getIdxWriter(), utils.getIdxWriterConfig(), fileType);
						}
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			}
	    		}
	    	});
			
			utils.close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		} finally {
			
			long end = System.currentTimeMillis();
			
			System.out.println("Indexing time = " + (end - start));
			
		}
	}
}