package application;
	
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {		
		
		try {			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("FaceRec2.fxml"));
			BorderPane rootElement = (BorderPane) loader.load();
			Scene scene = new Scene(rootElement, 900, 600);
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("Face Recognition");
			primaryStage.setMinWidth(1000);
			primaryStage.setMinHeight(600);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			FXController controller = loader.getController();
			controller.init();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
}
