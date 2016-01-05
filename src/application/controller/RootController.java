package application.controller;

import java.net.URL;
import java.util.ResourceBundle;

import application.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

public class RootController implements Initializable{

	@FXML
	Tab userTab;
	
	@FXML
	Tab recognitionTab;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {			
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/UserTab.fxml"));		
			userTab.setContent((BorderPane) loader.load());	
			loader = new FXMLLoader(Main.class.getResource("views/RecognitionTab.fxml"));		
			recognitionTab.setContent((BorderPane) loader.load());				
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
