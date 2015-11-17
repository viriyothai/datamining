package mining.Database;

import java.sql.*;
import java.util.ArrayList;

import mining.EntityLinkingStructure.CandidateStore;

public class GraphDatabase {

	private String entity1;
	private String entity2;
	private Connection con = null;
	private Statement stmt = null;
	
	public GraphDatabase(){
		
	}
	
	public GraphDatabase(String inputEntity1, String inputEntity2){
		entity1 = inputEntity1;
		entity2 = inputEntity2;
	}
	
	public ArrayList<CandidateStore> findCandidateFromDB(String entity) throws SQLException {
		// create connection in JDBC  
		System.out.println("Entity : " + entity);
		String query = "SELECT keyword FROM wikipage_en WHERE keyword like '"+ entity +"\\_%' or keyword = '" + entity + "' or keyword like '%\\_"+ entity +"'";
		//String query = "SELECT disambiguate FROM disambiguation_en WHERE disambiguate like '%\\_"+ entity +"\\_%'";
		PostgreSQLJDBC createConnection = new PostgreSQLJDBC();
		createConnection.setConnection();
		System.out.println("Connection Created Successfully !");
		con = createConnection.getConnection();
		stmt = con.createStatement();
		System.out.println("Run Query");
		ResultSet rs = stmt.executeQuery(query);
		
		ArrayList<CandidateStore> candidate = new ArrayList<>();
		while (rs.next()){
			candidate.add(new CandidateStore(rs.getString("keyword")));
		}
		
		if (con != null) {  
		    try {  
		     // closed the connection to db  
		     con.close();  
		    } catch (SQLException e) {  
		     e.printStackTrace();  
		    }
		}
		System.out.println("Finish Query"); 
		return candidate;  
	}
	
	/*
	public int countPageLink(String entity1, String entity2, String property) throws SQLException {
		// create connection in JDBC
		int countpage = 0;
		String query;
		
		PostgreSQLJDBC createConnection = new PostgreSQLJDBC();
		createConnection.setConnection();
		System.out.println("Connection Created Successfully !");
		con = createConnection.getConnection();
		stmt = con.createStatement();
		
		if (property.equals("Max") || property.equals("Min")) {
			query = "SELECT count(pagelink) AS countpage FROM pagelinks_en WHERE keyword like '"+ entity1 
					+"\\_%' or keyword = '"+ entity1 +"' or keyword like '%\\_"+ entity1 +"'";
			System.out.println("Run Query  :::::  " + entity1);
			ResultSet rs = stmt.executeQuery(query);
			
			int count1 = 0;
			while (rs.next()){
				count1 = rs.getInt("countpage");
			}
			
			query = "SELECT count(pagelink) AS countpage FROM pagelinks_en WHERE keyword like '"+ entity2 
					+"\\_%' or keyword = '"+ entity2 +"' or keyword like '%\\_"+ entity2 +"'";
			System.out.println("Run Query  :::::  " + entity2);
			rs = stmt.executeQuery(query);
			
			int count2 = 0;
			while (rs.next()){
				count2 = rs.getInt("countpage");
			}
		
			if(property.equals("Max")) {
				if(count1 > count2) {
					countpage = count1;
				} else {
					countpage = count2;
				}
			} else {
				if(count1 < count2) {
					countpage = count1;
				} else {
					countpage = count2;
				}
			}
		} else if (property.equals("Intersect")) {
			query = "select keyword, pagelink from pagelinks_en where (keyword like '"+ entity1 
					+"\\_%' or keyword = '"+ entity1 +"' or keyword like '%\\_"+ entity1 
					+"') and pagelink in ((select pagelink from pagelinks_en where keyword like '"
					+ entity2 +"\\_%' or keyword = '"+ entity2 +"' or keyword like '%\\_"+ entity2 +"'))";
			System.out.println("Run Query  :::::  " + entity1 + " && " + entity2);
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()){
				countpage = rs.getInt("countpage");
			}
		} else if (property.equals("All")){
			query = "select count(*) from pagelinks_en";
			System.out.println("Run Query  :::::  ALL");
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()){
				countpage = rs.getInt("countpage");
			}
		}
			
		
		if (con != null) {  
		    try {  
		     // closed the connection to db  
		     con.close();  
		    } catch (SQLException e) {  
		     e.printStackTrace();  
		    }
		}
		System.out.println("Finish Query"); 
		return countpage;  
	}
	
	*/

}
