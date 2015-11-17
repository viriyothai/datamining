package mining.EntityLinkingStructure;

import java.util.ArrayList;

public class EntityStore {

	private String entityMention;
	private ArrayList<CandidateStore> candidateEntities;
	private String sentence;
	
	public void setEntityMention(String inputEntity){
		entityMention = inputEntity;
	}
	
	public String getEntityMention() {
		return entityMention;
	}
	
	public void setSentence(String inputSentence){
		sentence = inputSentence;
	}
	
	public String getSentence(){
		return sentence;
	}
	
	public void setCandidateEntities(ArrayList<CandidateStore> candidateArr){
		candidateEntities = candidateArr;
	}
	
	public ArrayList<CandidateStore> getCandidateEntities(){
		return candidateEntities;
	}
	

}
