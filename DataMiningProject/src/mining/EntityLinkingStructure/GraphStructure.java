package mining.EntityLinkingStructure;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphStructure {
	
	private ArrayList<EntityStore> entity;
	private HashMap<String[], Double> edges;
	//private GraphStructure connect_entity;
	
	public GraphStructure(){
		edges = new HashMap<String[], Double>();
	}
	
	public GraphStructure(ArrayList<EntityStore> inputEntity){
		entity = inputEntity;
		edges = new HashMap<String[], Double>();
	}
	
	public void setEntity(ArrayList<EntityStore> inputEntity){
		entity = inputEntity;
	}
	
	public ArrayList<EntityStore> getEntity(){
		return entity;
	}
	
	public void setEdges(String[] key, double weight){
		System.out.println(key[0] + " + " + key[1] + " = " + weight);
		edges.put(key, weight);
	}
	
	public HashMap getAllEdges(){
		return edges;
	}
	
	public Double getEdge(String[] key){
		return edges.get(key);
	}
	
}
