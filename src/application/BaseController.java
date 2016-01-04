package application;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BaseController implements Initializable {

	class ClassifierPath {
		public static final String HAAR_FRONTAL_FACE_ALT = "resources/haarcascades/haarcascade_frontalface_alt.xml";
		public static final String LBP_FRONTAL_FACE = "resources/lbpcascades/lbpcascade_frontalface.xml";
	}

	@FXML
	private Button cameraButton;
	@FXML
	private ImageView originalFrame;
	@FXML
	private TitledPane classifierPane;
	
	protected ScheduledExecutorService timer; // a timer for acquiring the video stream	
	protected VideoCapture capture; // the OpenCV object that performs the video capture	
	protected boolean cameraActive; // a flag to change the button behavior	
	protected CascadeClassifier faceCascade; // face cascade classifier
	protected int absoluteFaceSize;
	protected int frameSize;
	protected FaceRecognizer recognizer = null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.absoluteFaceSize = 0;
		this.faceCascade.load(ClassifierPath.HAAR_FRONTAL_FACE_ALT);
		this.frameSize = 600;
	}
	
	@FXML
	protected void selectClassifier(Event event) {

		RadioButton btn = ((RadioButton) event.getSource());
		String classifierPath = "";

		switch (btn.getId()) {
			case "frontalFaceAltClassifier":
				classifierPath = ClassifierPath.HAAR_FRONTAL_FACE_ALT;
				break;
			case "":
				classifierPath = ClassifierPath.LBP_FRONTAL_FACE;
				break;
			default:
				classifierPath = ClassifierPath.HAAR_FRONTAL_FACE_ALT;
		}

		this.faceCascade.load(classifierPath); // load the classifier(s)
	}

	@FXML
	protected void startCamera() {
		
		originalFrame.setFitWidth(frameSize); // set a fixed width for the frame		
		originalFrame.setPreserveRatio(true); // preserve image ratio

		if (!this.cameraActive) {
			classifierPane.setDisable(true);			
			this.capture.open(0); // start the video capture
			
			if (this.capture.isOpened()) { 
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = () -> {
					Image imageToShow = grabFrame();
					originalFrame.setImage(imageToShow);
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);				
				this.cameraButton.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Failed to open the camera connection...");
			}
		} else {			
			this.cameraActive = false; 					// the camera is not active at this point			
			this.cameraButton.setText("Start Camera");  // update again the button content			
			this.classifierPane.setDisable(false); // enable classifiers radio buttons

			// stop the timer
			try {
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log the exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}

			this.capture.release();// release the camera			
			this.originalFrame.setImage(null); // clean the frame
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	protected Image grabFrame() {
		Image imageToShow = null;
		Mat frame = new Mat();

		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				if (!frame.empty()) {					
					this.detectAndDisplay(frame);	// face detection				
					imageToShow = mat2Image(frame); // convert the Mat object (OpenCV) to Image (JavaFX)
				}
			} catch (Exception e) {
				// log the (full) error
				System.err.println("ERROR: " + e);
			}
		}
		return imageToShow;
	}
	
	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 */
	protected Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}

	protected MatOfRect detectFaces(Mat frame) {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();

		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);

		// compute minimum face size (25% of the frame height in this case)
		if (this.absoluteFaceSize == 0) {
			int height = grayFrame.rows();
			if (Math.round(height * 0.30f) > 0) {
				this.absoluteFaceSize = Math.round(height * 0.30f);
			}
		}

		// detect faces
		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
		return faces;
	}
		
	
	/**
	 * Method for face detection and tracking
	 */
	protected void detectAndDisplay(Mat frame) {
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
			System.out.println("Recognized user: " + userId);
			
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
			Imgproc.putText(frame, "UserId: " + userId, facesArray[i].tl(), 2, 1, new Scalar(255, 255, 0));
		}
		
		

	}
}
