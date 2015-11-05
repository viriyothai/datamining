package mining.ExtractEntity;


import java.io.*;
import opennlp.tools.chunker.*;
import opennlp.tools.cmdline.*;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.coref.*;
import opennlp.tools.dictionary.*;
import opennlp.tools.doccat.*;
import opennlp.tools.formats.*;
import opennlp.tools.namefind.*;
import opennlp.tools.ngram.*;
import opennlp.tools.parser.*;
import opennlp.tools.postag.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.stemmer.*;
import opennlp.tools.tokenize.*;
import opennlp.tools.util.*;
import opennlp.maxent.*;
import opennlp.model.*;
import opennlp.perceptron.*;
import opennlp.uima.postag.*;
import java.util.List;

import net.didion.jwnl.data.list.PointerTargetTreeNodeList.FindNodeOperation;

import java.util.ArrayList;


public class ExtractEntity {
	private ArrayList<Parse> nounPhrases = new ArrayList<Parse>();
		
	
	public ArrayList<Parse> extractNounPhrases(String inputString) throws InvalidFormatException, IOException{
		Parse[] topParses = Parse(inputString);
		for (Parse p : topParses) {
			p.show();
			findNounPhrases(p);
		}
		return nounPhrases;
	}
	
	public Parse[] Parse(String inputString) throws InvalidFormatException, IOException {
		InputStream is = new FileInputStream("./src/Mining/ExtractEntity/en-parser-chunking.zip");
	 
		ParserModel model = new ParserModel(is);
	 
		Parser parser = ParserFactory.create(model);
	 
		String sentence = inputString;
		Parse[] topParses = ParserTool.parseLine(sentence, parser, 1);
	 
		is.close();
		
		return topParses;
	}
	
	public void findNounPhrases(Parse p) {
	    if (p.getType().equals("NP") || p.getType().equals("NNP") || p.getType().equals("NNPS")) {
	    	int count = 0;
	    	for (Parse child : p.getChildren()) {
		         if(child.getType().equals("NNP") || child.getType().equals("NNPS")) {
		        	 count++;
		         }
		    }
	    	if (count == p.getChildCount()) {
	    		nounPhrases.add(p);
	    	}
	    }
	    for (Parse child : p.getChildren()) {
	        findNounPhrases(child);
	    }
	}
	
	
	
	public ArrayList<ArrayList<String[]>> extractNameEntity(String paragraph) throws InvalidFormatException, IOException{
		String[] sentences = findSentences(paragraph);
		ArrayList<String[]> tokens = new ArrayList<String[]>();
		ArrayList<ArrayList<String[]>> nameEntities = new ArrayList<ArrayList<String[]>>();
		
		for(int i=0; i<sentences.length; i++) {
			tokens.add(findTokens(sentences[i]));
		}
		
		for(String[] entity : tokens){
			nameEntities.add(findNameEntity(entity));
		}
		return nameEntities;
	}
	
	public String[] findSentences(String paragraph) throws InvalidFormatException, IOException{
		InputStream is = new FileInputStream("./src/Mining/ExtractEntity/en-sent.zip");
		SentenceModel sentenceModel = new SentenceModel(is);
		SentenceDetectorME sdetector = new SentenceDetectorME(sentenceModel);
	 
		String sentencesArr[] = sdetector.sentDetect(paragraph);
		
		is.close();
		return sentencesArr;
	}
	
	public String[] findTokens(String inputSentences) throws InvalidFormatException, IOException{
		InputStream is = new FileInputStream("./src/Mining/ExtractEntity/en-token.zip");
		TokenizerModel tokenModel = new TokenizerModel(is);
		Tokenizer tokenizer = new TokenizerME(tokenModel);
	    
		String[] wordsArr;
	        
	    wordsArr = tokenizer.tokenize(inputSentences);
	    
	    is.close();
	    return wordsArr;
	}
	
	public ArrayList<String[]> findNameEntity(String[] inputWords) throws InvalidFormatException, IOException{
		//find Person
		InputStream is = new FileInputStream("./src/Mining/ExtractEntity/en-ner-person.zip");
		TokenNameFinderModel nameModel = new TokenNameFinderModel(is);
		NameFinderME nameFinder = new NameFinderME(nameModel);
	         
	    Span personSpans[] = nameFinder.find(inputWords);
	    
	    
	    //find location
	    is = new FileInputStream("./src/Mining/ExtractEntity/en-ner-location.zip");
        nameModel = new TokenNameFinderModel(is);
        nameFinder = new NameFinderME(nameModel);
        
        Span locationSpans[] = nameFinder.find(inputWords);
        
        
        //find organization
        is = new FileInputStream("./src/Mining/ExtractEntity/en-ner-organization.zip");
        nameModel = new TokenNameFinderModel(is);
        nameFinder = new NameFinderME(nameModel);
        
        Span organizationSpans[] = nameFinder.find(inputWords);
	    
        
        String[] nameEntity; 
        ArrayList<String[]> nameEntities = new ArrayList<String[]>();
        
        int i = 0;
        for(Span s: personSpans) {
        	nameEntity = new String[2]; 
        	nameEntity[1] = "";
            for(int j=s.getStart(); j<s.getEnd(); j++) {
                if(j != s.getStart())
                    nameEntity[1] += " ";
                nameEntity[1] += inputWords[j];
            }
            nameEntity[0] = "Person";
            nameEntities.add(nameEntity);
            i++;
        }

        for(Span s: locationSpans) {
        	nameEntity = new String[2]; 
        	nameEntity[1] = "";
            for(int j=s.getStart(); j<s.getEnd(); j++) {
                if(j != s.getStart())
                    nameEntity[1] += " ";
                nameEntity[1] += inputWords[j];
            }
            nameEntity[0] = "Location";
            nameEntities.add(nameEntity);
            i++;
        }
        
        for(Span s: organizationSpans) {
        	nameEntity = new String[2]; 
        	nameEntity[1] = "";
            for(int j=s.getStart(); j<s.getEnd(); j++) {
                if(j != s.getStart())
                    nameEntity[1] += " ";
                nameEntity[1] += inputWords[j];
            }
            nameEntity[0] = "Organization";
            nameEntities.add(nameEntity);
            i++;
        }

	    is.close();
	    return nameEntities;
	}
	
	public static void POSTag() throws IOException {
		POSModel model = new POSModelLoader().load(new File("./src/Mining/ExtractEntity/en-pos-maxent.zip"));
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		POSTaggerME tagger = new POSTaggerME(model);
	 
		String input = "Mike Stit loves Sarang Ho. They go to Microsoft together. Sometimes, Mike goes there alone.";
		ObjectStream<String> lineStream = new PlainTextByLineStream(
				new StringReader(input));
	 
		perfMon.start();
		String line;
		String whitespaceTokenizerLine[] = null;
	 
		String[] tags = null;
		while ((line = lineStream.read()) != null) {
			whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
					.tokenize(line);
			tags = tagger.tag(whitespaceTokenizerLine);
	 
			POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
			System.out.println(sample.toString());
				perfMon.incrementCounter();
		}
		perfMon.stopAndPrintFinalResult();
		
		// chunker
		InputStream is = new FileInputStream("./src/Mining/ExtractEntity/en-chunker.zip");
		ChunkerModel cModel = new ChunkerModel(is);
	 
		ChunkerME chunkerME = new ChunkerME(cModel);
		String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);
	 
		for (String s : result)
			System.out.println(s);
	 
		Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
		for (Span s : span)
			System.out.println(s.toString());
	}
	
	
}
