package mining.EntityLinkingStructure;

import java.util.ArrayList;

import opennlp.maxent.Counter;

public class CandidateStore {
	
	private String candidateName;
	private ArrayList<String> pagelink;
	private double priorProbability;
	private ArrayList<String> BagOfWords;
	private Counter countBag;
	
	public CandidateStore(String inputName){
		candidateName = inputName;
	}
	
	public String getCandidateName(){
		return candidateName;
	}
	
	public void setPageLink(ArrayList<String> inputpage){
		pagelink = inputpage;
	}
	
	public ArrayList<String> getPageLink(){
		return pagelink;
	}
	
	public void setPriorProbability(double inputProbability){
		priorProbability = inputProbability;
		System.out.println("P(" + candidateName + ") = " + priorProbability);
	}
	
	public double getPriorProbability(){
		return priorProbability;
	}
	
}
