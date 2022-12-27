package impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexOptions;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class DocxIndexerImpl {

	// File separator based on the system
    private static final char FILE_SEP = System.getProperty("file.separator").charAt(0);

    public static final FieldType NON_INDEX_FIELD = new FieldType();
    
    //set the non index attribute when class is created
    static {
        NON_INDEX_FIELD.setIndexOptions(IndexOptions.NONE);
        NON_INDEX_FIELD.setStored(true);
        NON_INDEX_FIELD.setTokenized(true);
        NON_INDEX_FIELD.freeze();
    }

    public Document createDocxDocument(File file) throws IOException{

        Document doc = new Document();
        String fileModifiedTimeStr = DateTools.timeToString(file.lastModified(),DateTools.Resolution.SECOND);
        String path = file.getPath();
        String docText = null;
        
        // Add the file path to a non index field
        doc.add(new Field("filepath",path, NON_INDEX_FIELD));
        
        // add the file attributes to index field
        doc.add(new StringField("lastmodified",fileModifiedTimeStr,Store.YES));

        //create UID for the indexer
        String uuid = path.replace(FILE_SEP,'-')+ fileModifiedTimeStr;
        
        // don't store this uuid into the index
        doc.add(new Field("uuidstring",uuid,TextField.TYPE_NOT_STORED));

        //read the docx file and add the data
        XWPFDocument docx;
		try {
			docx = new XWPFDocument(Files.newInputStream(Paths.get(file.getPath())));
			try (XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(docx)) {
				docText = xwpfWordExtractor.getText();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        StringReader reader = new StringReader(docText);
        doc.add(new TextField("Content", reader));
        
        return doc;
    }
}