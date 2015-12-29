package application;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FXController {

	class ClassifierPath {
		public static final String HAAR_FRONTAL_FACE_ALT = "resources/haarcascades/haarcascade_frontalface_alt.xml";
		public static final String LBP_FRONTAL_FACE = "resources/lbpcascades/lbpcascade_frontalface.xml";
	}
	
	// FXML buttons
	@FXML
	private Button cameraButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	@FXML
	private ImageView histogram;
	
	// radio button for enabling/disabling a classifier
	@FXML
	private RadioButton haarFrontalFaceAlt;
	@FXML
	private RadioButton lbpFrontalFace;
	
	@FXML
	private TitledPane classifierPane;

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture;
	// a flag to change the button behavior
	private boolean cameraActive;

	// face cascade classifier
	private CascadeClassifier faceCascade;
	private int absoluteFaceSize;
	

	/**
	 * Init the controller, at start time
	 */
	protected void init() {
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.absoluteFaceSize = 0;
		this.faceCascade.load(ClassifierPath.HAAR_FRONTAL_FACE_ALT);
	}

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera() {
		// set a fixed width for the frame
		originalFrame.setFitWidth(600);
		// preserve image ratio
		originalFrame.setPreserveRatio(true);

		if (!this.cameraActive) {
			// disable classifiers radio buttons
			classifierPane.setDisable(true);

			// start the video capture
			this.capture.open(0);

			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = () -> {
					Image imageToShow = grabFrame();
					originalFrame.setImage(imageToShow);
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content
				this.cameraButton.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Failed to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Camera");
			// enable classifiers radio buttons
			classifierPane.setDisable(false);

			// stop the timer
			try {
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log the exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}

			// release the camera
			this.capture.release();
			// clean the frame
			this.originalFrame.setImage(null);
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Image grabFrame() {
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				if (!frame.empty()) {
					// face detection
					this.detectAndDisplay(frame);

					// show the histogram
					this.showHistogram(frame);
					
					// convert the Mat object (OpenCV) to Image (JavaFX)
					imageToShow = mat2Image(frame);
				}

			} catch (Exception e) {
				// log the (full) error
				System.err.println("ERROR: " + e);
			}
		}

		return imageToShow;
	}

	/**
	 * Method for face detection and tracking
	 * 
	 * @param frame
	 *            it looks for faces in this frame
	 */
	private void detectAndDisplay(Mat frame) {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();

		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);

		// compute minimum face size (25% of the frame height in this case)
		if (this.absoluteFaceSize == 0) {
			int height = grayFrame.rows();
			if (Math.round(height * 0.25f) > 0) {
				this.absoluteFaceSize = Math.round(height * 0.25f);
			}
		}

		// detect faces
		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

		// each rectangle in faces is a face: draw them!
		Rect[] facesArray = faces.toArray();
		for (int i = 0; i < facesArray.length; i++)
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);

	}
	
	@FXML
	private void selectClassifier(Event event) {
		
		RadioButton btn = ((RadioButton) event.getSource());
		String classifierPath = "";
		
		switch(btn.getId()) {
			case "frontalFaceAltClassifier":
				classifierPath = ClassifierPath.HAAR_FRONTAL_FACE_ALT;
				break;
			case "":
				classifierPath = ClassifierPath.LBP_FRONTAL_FACE;
				break;				
			default:
				classifierPath = ClassifierPath.HAAR_FRONTAL_FACE_ALT;
		}
		
		// load the classifier(s)
		this.faceCascade.load(classifierPath);
	}
	
	

	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	private void showHistogram(Mat frame)
	{
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
		for (int i = 1; i < histSize.get(0, 0)[0]; i++)
		{
			
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
