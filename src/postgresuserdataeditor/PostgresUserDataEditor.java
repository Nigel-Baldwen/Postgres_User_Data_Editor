package postgresuserdataeditor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

public class PostgresUserDataEditor {

	private final String url = "jdbc:postgresql://localhost:5432/mydb";
    private final String user = "postgres";
    private final String password = "1S.e()c$u+r`e";
	
    /* Returns a Connection object to a database.
     * I opted to let the connection be an argument to various methods
     * rather than a field in its own right because I was envisioning
     * that a more complete/robust program would not all fit into just
     * one relatively short page/class setup. Therefore, the scheme of
     * passing around the connection reference seemed more fitting than
     * letting all methods access a private field. For example, maybe at
     * some point we would want the User class to perform user data updates
     * directly. With this setup, we can just cut/copy the current method as
     * is instead of having to set up some sort of reference within User
     * to point to a field of its own. */
    public Connection connect() {
        Connection connection = null;
        
        // Potentially, the connection attempt can fail,
        // so we use a try/catch block.
        try {
            connection = DriverManager.getConnection(url, user, password);
            // System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
        	
        	// More advanced error catching might be advised,
        	// but this suffices for a demo project
            System.out.println(e.getMessage());
        }

        return connection;
    }
    
    /* Typically, a database tries to avoid duplicate data.
     * That's why I chose to use a Set collection. I opted
     * for LinkedHashSet because it offers a nice compromise
     * between the speed of a HashSet without the chaotic ordering.
     * This way, we can get users added in according to a controlled
     * order if desired. For example, their order in the source database. */
    public static Set<User> getUserCollection (Connection connection) {
    	Set<User> userCollection = new LinkedHashSet<User>();
    	
    	try {
        	Statement statement = connection.createStatement();
        	String postgreSQLCommand = "SELECT * FROM users ORDER BY userid";
        	ResultSet resultSet = statement.executeQuery(postgreSQLCommand);
        	
        	while (resultSet.next()) {
				userCollection.add(new User(
						resultSet.getLong("userid"),
						resultSet.getString("fname"),
						resultSet.getString("lname"),
						resultSet.getTimestamp("signupdate")));
			}
		
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	
    	return userCollection;
    }
    
    /* This method can't reasonably know by itself which user to update with what data,
     * so it takes in information from the caller about what data to edit in the table.
     * The format is: colNameA = 'colDataA', colNameB = 'colDataB', etc.
     * The method returns true when the data is updated successfully and false otherwise. */
    public static boolean updateUser(long userID, String userData, Connection connection) {
    	
    	try {
        	Statement statement = connection.createStatement();
        	String postgreSQLCommand = "UPDATE users SET " + userData + " WHERE userid = " + userID;
        	return statement.execute(postgreSQLCommand);
		
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
		}
    }
    
    /* I made the decision that this method will return null if the requested User
     * doesn't exist. Alternately, we could have returned a dummy User of some sort,
     * broke out to a different method, or thrown a more elaborate exception. */
    public static User getUserByID(long userID, Connection connection) {
    	User resultUser;
    	
    	try {
        	Statement statement = connection.createStatement();
        	String postgreSQLCommand = "SELECT * FROM users WHERE userid = " + userID;
        	ResultSet resultSet = statement.executeQuery(postgreSQLCommand);
        	resultSet.next(); // Moves the row pointer to the first row.
        	
        	resultUser = new User(
        			resultSet.getLong("userid"),
					resultSet.getString("fname"),
					resultSet.getString("lname"),
					resultSet.getTimestamp("signupdate"));
        	
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            resultUser = null;
		}
    	
    	
    	return resultUser;
    }
    
    /* Simple main to demonstrate program functionality.
     * I didn't account for an empty database/table or failed login beyond the
     * try/catch clauses, but a more complete solution would probably do something
     * more substantial to cover those cases such as notifying the user. 
     * Console printouts only at this point.*/
	public static void main(String[] args) {
		
		// Create an instance of this class and connect to
		// the database with it.
		PostgresUserDataEditor pUDE = new PostgresUserDataEditor();
        Connection connection = pUDE.connect();
        
        updateUser(1, "fname = 'Frank'", connection);
        System.out.println(getUserByID(1, connection));
        updateUser(1, "fname = 'Dinosaur', lname = 'Rocket Lazer Beam'", connection);
        System.out.println(getUserByID(1, connection));
         
        System.out.println("\n\n\nFULL USER LIST:\n");
        Set<User> userCollection = getUserCollection(connection);
        System.out.println(userCollection);
	}

}

class User {
	
	private long userid;
	String fname, lname;
	Timestamp signupdate;
	
	// Test Constructor
	public User() {
		/* The no-argument constructor makes no sense in context.
		 * A user would almost certainly have a unique id.
		 * Thus, we can't reasonably create just any old user in
		 * practice with the limited information we have access to. 
		 * This is just for internal testing use. */
		
		
		// Junk test values
		userid = 999999999;
		fname = "John";
		lname = "Doe";
		signupdate = new Timestamp(Timestamp.valueOf("2020-1-1 12:30:15").getTime());
	}
	
	// Constructor
	public User(long userid, String fname, String lname, Timestamp signupdate) {
		this.userid = userid;
		this.fname = fname;
		this.lname = lname;
		this.signupdate = signupdate;
	}

	// To String
	@Override
	public String toString() {
		return "\nUser ID: " + userid + "\nFirst: " + fname + " Last: "
				+ lname + "\nSign Up Date: " + signupdate.toString();
	}

	// Getters & Setters
	public long getUserid() {
		return userid;
	}


	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getFname() {
		return fname;
	}


	public void setFname(String fname) {
		this.fname = fname;
	}


	public String getLname() {
		return lname;
	}


	public void setLname(String lname) {
		this.lname = lname;
	}


	public Timestamp getSignupdate() {
		return signupdate;
	}


	public void setSignupdate(Timestamp signupdate) {
		this.signupdate = signupdate;
	}

}
