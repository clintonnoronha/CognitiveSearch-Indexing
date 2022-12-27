package impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexOptions;

public class TextIndexerImpl {
	
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

    public Document createTextDocument(File file) throws IOException{

        Document doc = new Document();
        String fileModifiedTimeStr = DateTools.timeToString(file.lastModified(),DateTools.Resolution.SECOND);
        String path = file.getPath();
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String ls = System.getProperty("line.separator");
        
        // Add the file path to a non index field
        doc.add(new Field("filepath",path, NON_INDEX_FIELD));
        
        // add the file attributes to index field
        doc.add(new StringField("lastmodified",fileModifiedTimeStr,Store.YES));

        //create UID for the indexer
        String uuid = path.replace(FILE_SEP,'-')+ fileModifiedTimeStr;
        
        // don't store this uuid into the index
        doc.add(new Field("uuidstring",uuid,TextField.TYPE_NOT_STORED));

        //read the txt file and add the data
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((br.readLine()) != null) {
            	stringBuilder.append(line);
            	stringBuilder.append(ls);
            }
            // delete last newline separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
       
        StringReader reader = new StringReader(stringBuilder.toString());
        doc.add(new TextField("Content", reader));
        
        return doc;
    }
}