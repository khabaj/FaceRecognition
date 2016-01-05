package application.controller;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.face.Face;
import org.opencv.imgproc.Imgproc;

import application.DAO;
import application.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class UserController extends BaseController implements Initializable {
	
	@FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> userId;
    @FXML
    private TableColumn<User, String> name;
    @FXML
    HBox userImages;
	
	private ObservableList<User> userData; 
	private User selectedUser;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);
		this.frameSize = 500;
		
		userId.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
		name.setCellValueFactory(cellData -> cellData.getValue().nameProperty());		
		userData = FXCollections.observableArrayList();
		readUsers();
		
		usersTable.getSelectionModel().selectedItemProperty().addListener(
	                (observable, oldValue, newValue) -> {
	                	selectedUser = newValue; 
	                	showImages();
	                	}
	                );
	}
	
	@FXML
	private void addUser() {
		
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("New User");
		dialog.setHeaderText("Adding New User");
		dialog.setContentText("Please enter username:");

		Optional<String> result = null;
		while(result == null || result.isPresent() && result.get().isEmpty()) {
			result = dialog.showAndWait();
		}
		
		if (result.isPresent()) {
			Integer userId = DAO.addUser(result.get());
			userData.add(new User(userId, result.get()));
		}
	}
	
	private void readUsers() {
		userData.addAll(DAO.readUsers());
		usersTable.setItems(userData);
	}
	
	@FXML
	private void deleteUser() {		
		if (DAO.deleteUser(selectedUser.getUserId())) {
			int selectedIndex = usersTable.getSelectionModel().getSelectedIndex();
			usersTable.getItems().remove(selectedIndex);
		}
	}
	
	@FXML
	private void saveImage() {
		Image image = null;
		Mat frame = new Mat();
		Integer userId = null;
		
		if (selectedUser != null)
			userId = selectedUser.getUserId();

		if (userId != null && this.capture.isOpened()) {
			try {
				this.capture.read(frame);

				if (!frame.empty()) {
					MatOfRect faces = detectFaces(frame);
					Rect[] facesArray = faces.toArray();
					if (facesArray.length > 0 && facesArray.length < 2) {
						frame = frame.submat(facesArray[0]);
						Imgproc.resize(frame, frame, new Size(200, 200));

						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
						Imgproc.equalizeHist(frame, frame);
						image = mat2Image(frame);

						ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
						ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", byteOutput);
						byte[] res = byteOutput.toByteArray();

						DAO.saveImage(res, userId);

						Imgproc.resize(frame, frame, new Size(90, 90));
						image = mat2Image(frame);
						userImages.getChildren().add(new ImageView(image));
					}
				}
			} catch (Exception e) {
				System.err.println("ERROR: " + e);
			}
		}
	}

	private void showImages() {
		userImages.getChildren().clear();
		List<Image> imageList = DAO.getImagesByUser(selectedUser.getUserId());
		for (Image image : imageList) {
			userImages.getChildren().add(new ImageView(image));
		}
	}
}
