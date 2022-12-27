package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import impl.CustomAnalyzerImpl;
import impl.DocxIndexerImpl;
import impl.PDFIndexerImpl;
import impl.PptxIndexerImpl;
import impl.TextIndexerImpl;

public class LuceneIndexUtils {

	private Directory dir;
    private IndexWriterConfig idxWriterConfig;
    private IndexWriter idxWriter;
    private boolean createNew = true;

    public LuceneIndexUtils(String pathForIndex) {
    	
    	try {
    		
			dir = FSDirectory.open(Paths.get(pathForIndex));
			
			CustomAnalyzerImpl customAnalyzer = new CustomAnalyzerImpl();
	        
	        idxWriterConfig = new IndexWriterConfig(customAnalyzer.get());
	        
	        if (createNew) {
	        	idxWriterConfig.setOpenMode(OpenMode.CREATE);
	        } else {
	        	idxWriterConfig.setOpenMode(OpenMode.APPEND);
	        }
	        
	        idxWriter = new IndexWriter(dir, idxWriterConfig);
	        
	        setCreateNew(false);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	public boolean isCreateNew() {
		return createNew;
	}

	public void setCreateNew(boolean createNew) {
		this.createNew = createNew;
	}

	public Directory getDir() {
		return dir;
	}

	public void setDir(Directory dir) {
		this.dir = dir;
	}

	public IndexWriterConfig getIdxWriterConfig() {
		return idxWriterConfig;
	}

	public void setIdxWriterConfig(IndexWriterConfig idxWriterConfig) {
		this.idxWriterConfig = idxWriterConfig;
	}

	public IndexWriter getIdxWriter() {
		return idxWriter;
	}

	public void setIdxWriter(IndexWriter idxWriter) {
		this.idxWriter = idxWriter;
	}

	public void clearStore() throws Exception {
    	
        if (idxWriterConfig != null) {
        	
            IndexWriter idxLocalWriter = new IndexWriter(dir,idxWriterConfig);
            idxLocalWriter.deleteAll();
            idxLocalWriter.close();
        }
    }

    public void close() throws CorruptIndexException, Exception {

        if (idxWriter !=null && idxWriter.isOpen()) {
        	
            idxWriter.close();
        }
    }
    
    public void indexDocument(File file, IndexWriter idxWriter, IndexWriterConfig idxWriterConfig, String fileType) throws IOException {
    	
        if(idxWriterConfig == null ) {
            //
            System.err.println("IndexWriterConfig is not defined or initalized.");
            return; 
        }

        Document doc = null;
        
        switch (fileType) {
			case "application/pdf":
				// PDF Files Handling
				PDFIndexerImpl pdfIndexer = new PDFIndexerImpl();
		        doc = pdfIndexer.createPDFDocument(file);
				break;
			case "text/plain":
				// TXT Files Handling
				TextIndexerImpl txtIndexer = new TextIndexerImpl();
				doc = txtIndexer.createTextDocument(file);
				break;
			case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
				// DOCX Files Handling
				DocxIndexerImpl docxIndexer = new DocxIndexerImpl();
				doc = docxIndexer.createDocxDocument(file);
				break;
			case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
				// PPTX Files Handling
				PptxIndexerImpl pptxIndexer = new PptxIndexerImpl();
				doc = pptxIndexer.createPptxDocument(file);
				break;
				
			/*			
			case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
				// XLSX Files Handling
				
				break;
				
			*/	
				
			default:
				// Other Files which aren't processable
				break;	
        }

        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setTokenized(true);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);

        Term term = null;
        if(doc != null) {

            doc.add(new Field("File_attributes",file.getPath(),fieldType));
            term = new Term("File_attributes",file.getPath());
        }

        if(idxWriter!= null && (idxWriter.getConfig().getOpenMode() == OpenMode.CREATE || idxWriter.getConfig().getOpenMode() == OpenMode.APPEND)) {
            idxWriter.addDocument(doc);
        }else {
            if (idxWriter != null && term!=null) {
                idxWriter.deleteDocuments(term);
                idxWriter.commit();
            }
        }
    }
}
