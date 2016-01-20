package application.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgproc.Imgproc;

import application.DAO;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class RecognitionController extends BaseController implements Initializable{

	@FXML
	private ImageView histogram;
	
	@FXML
	private TitledPane recognitionAlgPane;
	@FXML
	ToggleGroup recAlgorithm;
	
	protected FaceRecognizer recognizer = null;
	String recognitionDataFileName;
	Map <Integer, String> usersMap;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);
		recognizer = Face.createEigenFaceRecognizer();
		//recognizer = Face.createEigenFaceRecognizer(100, 10000);
		recognitionDataFileName = "EigenFacesData.yml";
	}
	
	@Override
	protected Image grabFrame() {
		Image imageToShow = null;
		Mat frame = new Mat();

		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				if (!frame.empty()) {					
					this.recognize(frame);	// face detection	
					this.showHistogram(frame);
					imageToShow = mat2Image(frame); // convert the Mat object (OpenCV) to Image (JavaFX)
				}

			} catch (Exception e) {
				// log the (full) error
				System.err.println("ERROR: " + e);
			}
		}
		return imageToShow;
	}
	
	protected void recognize(Mat frame) {
		MatOfRect faces = detectFaces(frame);
		Mat grayFrame = new Mat();
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(grayFrame, grayFrame);
		
		// each rectangle in faces is a face: draw them!
		Rect[] facesArray = faces.toArray();
		for (int i = 0; i < facesArray.length; i++) {
			
			Mat faceToRecognize = grayFrame.submat(facesArray[i]);
			Imgproc.resize(faceToRecognize, faceToRecognize, new Size(200,200));
			int userId = recognizer.predict(faceToRecognize);			
			
			String text = usersMap.get(userId) != null ? usersMap.get(userId) +"("+userId+")" : "unknown";
			
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
			Imgproc.putText(frame, "User: " + text, facesArray[i].tl(), 2, 0.8, new Scalar(255, 255, 0));
		}
	}
	
	@FXML
	@Override
	protected void startCamera() {
		if (!this.cameraActive) {
			recognitionAlgPane.setDisable(true);
			train();
		}
		else {
			recognitionAlgPane.setDisable(false);
		}
		super.startCamera();
	}
	
	
	private void train() {
		List<Mat> images = new ArrayList<>();
		Mat labels = new Mat();
		DAO.getTrainingData(images, labels);
		recognizer.train(images, labels);
		usersMap = DAO.readUsersMap();
		
		//recognizer.save(recognitionDataFileName);
	}
				
	/*private void loadRecognizer() {		
		try {
			recognizer.load(recognitionDataFileName);
		} catch (Exception e) {
			train();
		}				
	}*/
	
	@FXML
	private void selectRecAlgorithm(Event event) {
		RadioButton btn = ((RadioButton) event.getSource());

		switch (btn.getId()) {
			case "eigenfacesAlgorithm":
				recognizer = Face.createEigenFaceRecognizer();
				recognitionDataFileName = "EigenFacesData.yml";
				break;
			case "fisherfacesAlgorithm":
				recognizer = Face.createFisherFaceRecognizer();
				recognitionDataFileName = "FisherFacesData.yml";
				break;
			case "lbpAlgorithm":
				recognizer = Face.createLBPHFaceRecognizer();
				recognitionDataFileName = "LBPHFaces.yml";
				break;
			default:
				recognizer = Face.createEigenFaceRecognizer();
				recognitionDataFileName = "EigenFacesData.yml";
		}
	}
	
	private void showHistogram(Mat frame) {
		// split the frames in multiple images
		List<Mat> images = new ArrayList<Mat>();
		Core.split(frame, images);

		// set the number of bins at 256
		MatOfInt histSize = new MatOfInt(256);
		// only one channel
		MatOfInt channels = new MatOfInt(0);
		// set the ranges
		MatOfFloat histRange = new MatOfFloat(0, 256);

		// compute the histograms for the B, G and R components
		Mat hist_b = new Mat();
		Mat hist_g = new Mat();
		Mat hist_r = new Mat();

		Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_b, histSize, histRange, false);
		Imgproc.calcHist(images.subList(1, 2), channels, new Mat(), hist_g, histSize, histRange, false);
		Imgproc.calcHist(images.subList(2, 3), channels, new Mat(), hist_r, histSize, histRange, false);

		// draw the histogram
		int hist_w = 150; // width of the histogram image
		int hist_h = 150; // height of the histogram image
		int bin_w = (int) Math.round(hist_w / histSize.get(0, 0)[0]);

		Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(0, 0, 0));

		// normalize the result to [0, histImage.rows()]
		Core.normalize(hist_b, hist_b, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
		Core.normalize(hist_g, hist_g, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
		Core.normalize(hist_r, hist_r, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());

		// effectively draw the histogram(s)
		for (int i = 1; i < histSize.get(0, 0)[0]; i++) {

			Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_b.get(i - 1, 0)[0])),
					new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);
			Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_g.get(i - 1, 0)[0])),
					new Point(bin_w * (i), hist_h - Math.round(hist_g.get(i, 0)[0])), new Scalar(0, 255, 0), 2, 8, 0);
			Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_r.get(i - 1, 0)[0])),
					new Point(bin_w * (i), hist_h - Math.round(hist_r.get(i, 0)[0])), new Scalar(0, 0, 255), 2, 8, 0);
		}

		// display the histogram...
		Image histImg = mat2Image(histImage);
		this.histogram.setImage(histImg);

	}

	
}
