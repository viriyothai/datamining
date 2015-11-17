package mining.EntityLinkingControl;


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
	private ArrayList<String> nameEntitiesAll = new ArrayList<String>();	
	
	
	public void extractNameEntities(String inputString) throws InvalidFormatException, IOException{
		//System.out.println(inputString);
		Parse[] topParses = Parse(inputString);
		
		for (Parse p : topParses) {
			p.show();
			findNounPhrases(p);
			
			/*
			String[] words = new String[nounPhrases.size()];
			int i = 0;
			for(Parse n : nounPhrases){
				words[i] = replaceExtraChar(n.toString());
				i++;
			}
			
			ArrayList<String> name = new ArrayList<String>();
			name.clear();
			name = findNameEntity(words);
			if(name.size() != 0)
				nameEntitiesAll.addAll(name);
			for(String namee : nameEntitiesAll)
				System.out.println(namee);
			*/
		}
		for (Parse noun: nounPhrases) {
			nameEntitiesAll.add(noun.toString());
		}
	}
	
	public ArrayList<String> getNameEntities(){
		return nameEntitiesAll;
	}
	
	public Parse[] Parse(String inputString) throws InvalidFormatException, IOException {
		InputStream is = new FileInputStream("./src/Mining/EntityLinkingControl/en-parser-chunking.zip");
	 
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
	
	public String[] findSentences(String paragraph) throws InvalidFormatException, IOException{
		InputStream is = new FileInputStream("./src/Mining/EntityLinkingControl/en-sent.zip");
		SentenceModel sentenceModel = new SentenceModel(is);
		SentenceDetectorME sdetector = new SentenceDetectorME(sentenceModel);
	 
		String sentencesArr[] = sdetector.sentDetect(paragraph);
		
		is.close();
		return sentencesArr;
	}
	
	public String[] findTokens(String inputSentences) throws InvalidFormatException, IOException{
		InputStream is = new FileInputStream("./src/Mining/EntityLinkingControl/en-token.zip");
		TokenizerModel tokenModel = new TokenizerModel(is);
		Tokenizer tokenizer = new TokenizerME(tokenModel);
	    
		String[] wordsArr;
	        
	    wordsArr = tokenizer.tokenize(inputSentences);
	    
	    is.close();
	    return wordsArr;
	}
	
	public ArrayList<String> findNameEntity(String[] inputWords) throws InvalidFormatException, IOException{
		//find Person
		InputStream is = new FileInputStream("./src/Mining/EntityLinkingControl/en-ner-person.zip");
		TokenNameFinderModel nameModel = new TokenNameFinderModel(is);
		NameFinderME nameFinder = new NameFinderME(nameModel);
	         
	    Span personSpans[] = nameFinder.find(inputWords);
	    
	    
	    //find location
	    is = new FileInputStream("./src/Mining/EntityLinkingControl/en-ner-location.zip");
        nameModel = new TokenNameFinderModel(is);
        nameFinder = new NameFinderME(nameModel);
        
        Span locationSpans[] = nameFinder.find(inputWords);
        
        
        //find organization
        is = new FileInputStream("./src/Mining/EntityLinkingControl/en-ner-organization.zip");
        nameModel = new TokenNameFinderModel(is);
        nameFinder = new NameFinderME(nameModel);
        
        Span organizationSpans[] = nameFinder.find(inputWords);
	    
        
        String nameEntity; 
        ArrayList<String> nameEntities = new ArrayList<String>();
        
        boolean contain = false;
        for(Span s: personSpans) {
        	nameEntity = "";
            for(int j=s.getStart(); j<s.getEnd(); j++) {
                if(j != s.getStart())
                    nameEntity += " ";
                nameEntity += inputWords[j];
            }
            contain = false;
            for (String name : nameEntities)
            	if (name.equals(nameEntity))
            		contain = true;
            if(!contain) {
            	nameEntities.add(nameEntity);
            	System.out.println(nameEntity);
            }
        }
        
        for(Span s: locationSpans) {
        	nameEntity = "";
            for(int j=s.getStart(); j<s.getEnd(); j++) {
                if(j != s.getStart())
                    nameEntity += " ";
                nameEntity += inputWords[j];
            }
            contain = false;
            for (String name : nameEntities)
            	if (name.equals(nameEntity))
            		contain = true;
            if(!contain) {
            	nameEntities.add(nameEntity);
            	System.out.println(nameEntity);
            }
        }
        
        for(Span s: organizationSpans) {
        	nameEntity = "";
            for(int j=s.getStart(); j<s.getEnd(); j++) {
                if(j != s.getStart())
                    nameEntity += " ";
                nameEntity += inputWords[j];
            }
            contain = false;
            for (String name : nameEntities)
            	if (name.equals(nameEntity))
            		contain = true;
            if(!contain) {
            	nameEntities.add(nameEntity);
            	System.out.println(nameEntity);
            }
        }

	    is.close();
	    
	    return nameEntities;
	}
	
	public static void POSTag() throws IOException {
		POSModel model = new POSModelLoader().load(new File("./src/Mining/EntityLinkingControl/en-pos-maxent.zip"));
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
		InputStream is = new FileInputStream("./src/Mining/EntityLinkingControl/en-chunker.zip");
		ChunkerModel cModel = new ChunkerModel(is);
	 
		ChunkerME chunkerME = new ChunkerME(cModel);
		String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);
	 
		for (String s : result)
			System.out.println(s);
	 
		Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
		for (Span s : span)
			System.out.println(s.toString());
	}
	
	public String replaceExtraChar(String input){
		input = input.replace("'s", "");
		input = input.replaceAll("[@#\\%,\\.]", "");
		return input;
	}
	
}
