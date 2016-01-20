package application;
	
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {		
		
		try {			
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/RootLayout.fxml"));
			TabPane rootElement = (TabPane) loader.load();
			Scene scene = new Scene(rootElement, 900, 600);
			
			scene.getStylesheets().add(Main.class.getResource("css/application.css").toExternalForm());
			primaryStage.setTitle("Face Recognition");
			primaryStage.setMinWidth(1000);
			primaryStage.setMinHeight(600);
			primaryStage.setScene(scene);
			primaryStage.show();			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			String opencvpath = System.getProperty("user.dir") + "\\";
			System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
			launch(args);
		} catch (Exception e) {			
		} finally {
			SQLiteConnector.closeConnection();
		}
	}
}
