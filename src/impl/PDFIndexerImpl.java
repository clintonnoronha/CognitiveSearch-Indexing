package impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFIndexerImpl {

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

    public Document createPDFDocument(File pdfFile) throws IOException{

        Document doc = new Document();
        String fileModifiedTimeStr = DateTools.timeToString(pdfFile.lastModified(),DateTools.Resolution.SECOND);
        String path = pdfFile.getPath();
        
        // Add the file path to a non index field
        doc.add(new Field("filepath",path, NON_INDEX_FIELD));
        
        // add the file attributes to index field
        doc.add(new StringField("lastmodified",fileModifiedTimeStr,Store.YES));

        //create UID for the indexer
        String uuid = path.replace(FILE_SEP,'-')+ fileModifiedTimeStr;
        
        // don't store this uuid into the index
        doc.add(new Field("uuidstring",uuid,TextField.TYPE_NOT_STORED));

        //read the pdf file using pdfbox and add the data
        try(FileInputStream inputStream = new FileInputStream(pdfFile)) {
            parsePDFAndAddContentToDocument(doc,inputStream,path);
        }
        return doc;
    }

    private void parsePDFAndAddContentToDocument(Document doc, FileInputStream inputStream, String path) throws IOException {

        //PDDocument implements closable
        // also throws load() throws io exception
        try (PDDocument pdfDocument = PDDocument.load(inputStream)){

            StringWriter strWriter= new StringWriter();
            PDFTextStripper txtStripper= new PDFTextStripper();
            txtStripper.writeText(pdfDocument, strWriter);
            
            //read the content and add to index as text field
            StringReader reader = new StringReader(strWriter.getBuffer().toString());
            //System.out.println("read content:- "+strWriter.getBuffer().toString());
            doc.add(new TextField("Content", reader));
            
            // NOTE: if the pdf is password protected then it will throw 
            // invalidpasswordExcepton and cannot index the file
            
            /*
            
            // Metadata of the PDF that can be indexed
            PDDocumentInformation metaInfo = pdfDocument.getDocumentInformation();

            if(metaInfo != null) {
                doc.add(new TextField("PDFAuthor", metaInfo.getAuthor(),Store.YES));
                doc.add(new TextField("PDFCreationDate",metaInfo.getCreationDate().toString(),Store.YES));
            }
            
            */
            
        }
    }
}