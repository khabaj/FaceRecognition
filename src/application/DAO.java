package application;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.scene.image.Image;

public class DAO {

	public static Integer addUser(String username) {

		Connection connection = SQLiteConnection.getConnection();
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
		Connection connection = SQLiteConnection.getConnection();
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
	
	public static boolean deleteUser(Integer userId) {
		Connection connection = SQLiteConnection.getConnection();
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
	
	public static void saveImage(byte[] image, Integer userId) {
		Connection connection = SQLiteConnection.getConnection();
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("INSERT INTO image (image,userId) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setBytes(1, image);
			statement.setInt(2, userId);			
			statement.executeUpdate();	
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
	
	protected static Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	public static List<Image> getImagesByUser(Integer userId) {
		Connection connection = SQLiteConnection.getConnection();
		PreparedStatement statement = null;
		List<Image> imageList = new ArrayList<>();
		try {
			statement = connection.prepareStatement("SELECT image from image where userId = ?");
			statement.setInt(1, userId);
			
			ResultSet resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				byte[] bytes = resultSet.getBytes(1);
				Image image = new Image(new ByteArrayInputStream(bytes),90,90,false,false);				 
				imageList.add(image);
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
		return imageList;
	}
	
	public static void getTrainingData(List<Mat> images, Mat labels) {
		
		Connection connection = SQLiteConnection.getConnection();
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
