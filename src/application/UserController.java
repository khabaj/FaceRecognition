package application;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class UserController extends BaseController implements Initializable{
	
	@FXML
	VBox userImages;
	
	@FXML
	TextField userNameTextField;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {	
		super.initialize(location, resources);
		this.frameSize = 500;
	}
	
	@FXML
	private void savePhoto() {
		Image image = null;
		Mat frame = new Mat();

		if (!userNameTextField.getText().isEmpty() && this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				if (!frame.empty()) {	
					MatOfRect faces = detectFaces(frame);	
					Rect[] facesArray = faces.toArray();
					if(facesArray.length > 0){
						frame = frame.submat(facesArray[0]);
						Imgproc.resize(frame, frame, new Size(200,200));
						
						Path path = Paths.get(System.getProperty("user.dir"));
						File dir = new File(path + "\\users\\" + userNameTextField.getText());
						dir.mkdirs();
						
						String photoName = userNameTextField.getText() + System.currentTimeMillis() + ".png";
						Path pathToPhoto = Paths.get(dir.getAbsolutePath(), photoName);						
						Imgcodecs.imwrite(pathToPhoto.toString(), frame);
						
						Path relativePath = path.relativize(Paths.get(pathToPhoto.toString()));
						
						//saveToCSV(relativePath.toString(), "1");
						
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
	
	
	void saveToCSV(String imagePath, String imageClass) {
		
	}
}
