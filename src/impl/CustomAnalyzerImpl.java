package impl;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;

public class CustomAnalyzerImpl {

	public Analyzer get() throws IOException {
		Analyzer analyzer = CustomAnalyzer.builder()
				.withTokenizer("standard")
				.addTokenFilter("stop")
				.addTokenFilter("lowercase")
				.build();
		
		return analyzer;
	}
}