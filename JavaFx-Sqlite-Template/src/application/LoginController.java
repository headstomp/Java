package application;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class LoginController implements Initializable {
	public LoginModel loginModel = new LoginModel();
	
	@FXML
	private Label isConnected;
	@FXML
	private TextField txtUserName;
	@FXML
	private TextField txtPassword;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		if(loginModel.isDbConnected()) {
			isConnected.setText("Database Available");
		}else {
			isConnected.setText("No Database");
		}
		
	}
	
	public void Login(ActionEvent event) {
		try {
			if(loginModel.isLoggedIn(txtUserName.getText(), txtPassword.getText())) {
				
				isConnected.setText("user/password accepted");
				((Node)event.getSource()).getScene().getWindow().hide();
				Stage primaryStage = new Stage();
				FXMLLoader loader = new FXMLLoader();
				Pane root = loader.load(getClass().getResource("/application/Settings.fxml").openStream());
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				
			}else {
				isConnected.setText("user or pass incorrect");
			}
		} catch (SQLException e) {
			isConnected.setText("user or pass incorrect");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
