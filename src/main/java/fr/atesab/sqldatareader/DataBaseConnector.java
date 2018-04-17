package fr.atesab.sqldatareader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseConnector {

	private Connection connection;
	private String username;
	private String password;
	private String url;
	private String table;

	public DataBaseConnector(String username, String password, String url, String table) {
		this.username = username;
		this.password = password;
		this.url = url;
		this.table = table;
	}

	public Connection getConnection() {
		return connection;
	}

	public ResultSet loadData(String uuid) {
		try {
			if (connection.isClosed() && !openConnection())
				return null;
			ResultSet result = connection.prepareStatement("SELECT * FROM " + table + " WHERE uuid='" + uuid + "';")
					.executeQuery();
			if (result.next()) // use row
				return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean openConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + url, username, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
