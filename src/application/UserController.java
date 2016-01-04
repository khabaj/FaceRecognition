package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.face.Face;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class UserController extends BaseController implements Initializable{
	
	@FXML
	VBox userImages;
	
	@FXML
	TextField userNameTextField;
	
	@FXML
	Button trainingButton;
	
	private final String COMMA_DELIMITER = ",";
	private final String NEW_LINE_SEPARATOR = "\n";
	private String fileName = "users.csv";
	
	Path path;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {	
		super.initialize(location, resources);
		this.frameSize = 500;
		path = Paths.get(System.getProperty("user.dir"));
	}
	
	@FXML
	private void savePhoto() {
		Image image = null;
		Mat frame = new Mat();
		String userName = userNameTextField.getText();
		
		if (!userName.isEmpty() && this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				if (!frame.empty()) {	
					MatOfRect faces = detectFaces(frame);	
					Rect[] facesArray = faces.toArray();
					if(facesArray.length > 0 && facesArray.length < 2){
						frame = frame.submat(facesArray[0]);
						Imgproc.resize(frame, frame, new Size(200,200));
						
						File dir = new File(path + "\\users\\" + userName);
						dir.mkdirs();
						
						String photoName = userName + System.currentTimeMillis() + ".png";
						Path pathToPhoto = Paths.get(dir.getAbsolutePath(), photoName);						
						Imgcodecs.imwrite(pathToPhoto.toString(), frame);
						
						Path relativePath = path.relativize(Paths.get(pathToPhoto.toString()));
						
						saveToCSV(relativePath.toString(), userName, "1");
						
						Imgproc.resize(frame, frame, new Size(100,100));
						image = mat2Image(frame); 
						userImages.getChildren().add(new ImageView(image));
					}
					
					
				}
			} catch (Exception e) {
				// log the (full) error
				System.err.println("ERROR: " + e);
			}
		}
	}
	
	@FXML
	private void train() {
		recognizer = Face.createLBPHFaceRecognizer();
		List<Mat> images = new ArrayList<>();
		
		Mat labels = new Mat();
		readCSV(images, labels);
		
		recognizer.train(images, labels);
		recognizer.save("lbph.yml");
	}
	
	private void readCSV(List<Mat> images, Mat labels) {
		
		BufferedReader fileReader = null;
		String line = "";
		 
		try {
			fileReader = new BufferedReader(new FileReader(fileName));			
			while ((line = fileReader.readLine()) != null) {
				String[] tokens = line.split(COMMA_DELIMITER);
				if (tokens.length > 0) {
					Mat image = Imgcodecs.imread(tokens[0], 0);
					if (image != null && !image.empty()) {
						images.add(image);						
						Mat label = new Mat(1, 1, CvType.CV_32SC1);
						label.put(0, 0, Integer.parseInt(tokens[2]));
						labels.push_back(label);
					}
				}
			}
			
		} catch(Exception e) {
			
		} finally {
			try {
				fileReader.close();
				} catch (IOException e) {
					System.out.println("Error while closing fileReader !!!");
					e.printStackTrace();
				}
			}
		}
	
	
	private void saveToCSV(String imagePath, String userName, String imageClass) {
		
		
		FileWriter fileWriter = null;
		
		try {
			fileWriter = new FileWriter(fileName, true);
			fileWriter.append(imagePath);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(userName);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(imageClass);
			fileWriter.append(NEW_LINE_SEPARATOR);
			
			
		}catch (Exception e) {
			System.out.println("Error while saving CSV file !!!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
		}
	}
}
