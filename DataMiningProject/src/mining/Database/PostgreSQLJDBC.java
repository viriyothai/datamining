package mining.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLJDBC {

	// defining driver name, mysql-jdbc driver in this case  
	String driverName = "org.postgresql.Driver";  
	Connection con = null;  
		  
	// database url string with hostname and port on which db is running  
	String url = "jdbc:postgresql://localhost:5432/";  
		  
	public void setConnection() {  

		 String dbName = "DBpedia";
		 String username = "postgres";
		 String password = "password";
		 
		 // creating connection url  
		 String connectionUrl = url + dbName;  
		 
		 try {  
			 // registers the specified driver class into memory  
			 Class.forName(driverName);  
		 } catch (ClassNotFoundException e) {  
			 e.printStackTrace();  
		 }  
		  
		 try {  
			 // returns a connection object by selecting an appropriate driver  
			 // for specified connection URL  
			 con = DriverManager.getConnection(connectionUrl, username, password);  
		 } catch (SQLException e) {  
			 e.printStackTrace();  
		 }  
		    
		   
	}
	
	public Connection getConnection(){
		return con;
	}
}
