package application;
import java.sql.*;

public class LoginModel {
	Connection connection;
	public LoginModel() {
		connection = SqliteConnection.Connector();
		if(connection == null) System.exit(1);
		
	}
	
	public boolean isDbConnected() {
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public boolean isLoggedIn(String user, String pass) throws SQLException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "select * from user where username = ? and password = ?";
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, user);
			preparedStatement.setString(2, pass);
			
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return true;
			}else {
				return false;
			}
			
		}catch (Exception e){
			return false;
		}finally {
			preparedStatement.close();
			resultSet.close();
		}
	}
}


