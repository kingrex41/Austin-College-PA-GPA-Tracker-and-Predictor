package pagpa.cs380;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class LoginPageController implements Initializable {
	
	@FXML
	private Button loginButton;
	
	@FXML
	private TextField usernameField;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private Label incorrectInfo;
	
	@FXML
	private HBox incorrectInfoBox;
	
	public void login() {
		String username = usernameField.getText().trim();
		String password = passwordField.getText().trim();
		
		if("admin".equals(username) && "admin".equals(password)) {
			incorrectInfoBox.setVisible(false);
			incorrectInfo.setVisible(false);
			try {
				App.switchScene("primary");
				App.setRoot("primary",true); // switch page and expand window
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		} else if ("student".equals(username) && "student".equals(password)) {
			incorrectInfoBox.setVisible(false);
			incorrectInfo.setVisible(false);
			try {
				App.switchScene("studentdash2");
				StudentdashController controller = (StudentdashController) App.getController();
				if (controller != null) {
					// Create a blank student (empty fields)
					pagpa.cs380.Models.Student blankStudent = new pagpa.cs380.Models.Student(0, "", "", "", "", "", false);
					controller.setStudent(blankStudent);
					controller.finishInit();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			incorrectInfo.setText("Incorrect username or password!");
			incorrectInfo.setVisible(true);
			incorrectInfoBox.setVisible(true);
			System.err.println("Incorrect Username or Password! Username Entered: '"+username+"' , Password Entered: '"+password+"'");
			usernameField.clear(); // Clears username box after incorrect input
			passwordField.clear(); // Clears password box after incorrect input
			
			/*
			 * position cursor inside username text field
			 */
			Platform.runLater(() -> usernameField.requestFocus());
			
		}
	}


	/*
	 * during initialization, we make it so the user does not have to 
	 * use the mouse to click into the next field.   and when they hit
	 * return in the password field, we attempt to login.
	 * 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	
		usernameField.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent args) {
		       passwordField.requestFocus();
		    }
		});
		
		passwordField.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent args) {
		    	login();
		    }
		});
		
		/*
		 * as the final act of initialization, we position the focus
		 * in the username field automagically.
		 */
		Platform.runLater(() -> usernameField.requestFocus());
		
	}

}
