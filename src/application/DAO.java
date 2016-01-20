package application;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.scene.image.Image;

public class DAO {

	public static Integer addUser(String username) {

		Connection connection = SQLiteConnector.getConnection();
		PreparedStatement statement = null;
		Integer userId = null;
		try {
			statement = connection.prepareStatement("INSERT INTO user (name) VALUES (?)",
					Statement.RETURN_GENERATED_KEYS);

			statement.setString(1, username);
			statement.executeUpdate();
			ResultSet generatedKeys = statement.getGeneratedKeys();

			if (generatedKeys.next())
				userId = generatedKeys.getInt(1);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return userId;
	}

	public static List<User> readUsers() {
		Connection connection = SQLiteConnector.getConnection();
		PreparedStatement statement = null;
		List<User> users = new ArrayList<>();
		try {
			statement = connection.prepareStatement("SELECT userId, name from user");

			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				Integer userId = resultSet.getInt(1);
				String name = resultSet.getString(2);
				users.add(new User(userId, name));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return users;
	}
	
	public static Map<Integer, String> readUsersMap() {
		Connection connection = SQLiteConnector.getConnection();
		PreparedStatement statement = null;
		Map<Integer, String> map = new HashMap<>();
		try {
			statement = connection.prepareStatement("SELECT userId, name from user");

			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				Integer userId = resultSet.getInt(1);
				String name = resultSet.getString(2);
				map.put(userId, name);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	
	public static boolean deleteUser(Integer userId) {
		Connection connection = SQLiteConnector.getConnection();
		PreparedStatement statement = null;
		boolean result = true;
		try {
			statement = connection.prepareStatement("DELETE from user where userId = ?");
			statement.setInt(1, userId);
			
			statement.execute();	
			
		} catch (SQLException e) {
			result = false;
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static Integer saveImage(byte[] image, Integer userId) {
		Connection connection = SQLiteConnector.getConnection();
		PreparedStatement statement = null;
		Integer imageId = null;
		
		try {
			statement = connection.prepareStatement("INSERT INTO image (image,userId) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setBytes(1, image);
			statement.setInt(2, userId);			
			statement.executeUpdate();	
			
			ResultSet generatedKeys = statement.getGeneratedKeys();

			if (generatedKeys.next())
				imageId = generatedKeys.getInt(1);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return imageId;
	}
	
	public static boolean deleteImage(Integer imageId) {
		Connection connection = SQLiteConnector.getConnection();
		PreparedStatement statement = null;
		boolean result = true;
		try {
			statement = connection.prepareStatement("DELETE from image where imageId = ?");
			statement.setInt(1, imageId);
			
			statement.execute();	
			
		} catch (SQLException e) {
			result = false;
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static Map<Integer, Image> getImagesByUser(Integer userId) {
		Connection connection = SQLiteConnector.getConnection();
		PreparedStatement statement = null;
		Map<Integer, Image> imageMap = new HashMap<Integer, Image>();
		try {
			statement = connection.prepareStatement("SELECT imageId, image from image where userId = ?");
			statement.setInt(1, userId);
			
			ResultSet resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				Integer imageId = resultSet.getInt(1);
				byte[] bytes = resultSet.getBytes(2);
				Image image = new Image(new ByteArrayInputStream(bytes),90,90,false,false);				 
				imageMap.put(imageId, image);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return imageMap;
	}
	
	public static void getTrainingData(List<Mat> images, Mat labels) {
		
		Connection connection = SQLiteConnector.getConnection();
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("SELECT image, userID from image");
			
			ResultSet resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				byte[] bytes = resultSet.getBytes(1);	
				Integer userId = resultSet.getInt(2);
				Mat image = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
				if (image != null && !image.empty()) {
					images.add(image);
					Mat label = new Mat(1, 1, CvType.CV_32SC1);
					label.put(0, 0, (userId));
					labels.push_back(label);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
