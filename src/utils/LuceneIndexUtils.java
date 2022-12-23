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
import impl.PDFIndexerImpl;

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
    
    public void indexPDFDocument(File file, IndexWriter idxWriter, IndexWriterConfig idxWriterConfig) throws IOException {
    	
        if(idxWriterConfig == null ) {
            //
            System.err.println("IndexWriterConfig is not defined or initalized.");
            return; 
        }

        Document doc = null;
        
        PDFIndexerImpl indexer = new PDFIndexerImpl();
        doc = indexer.createDocument(file);

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
