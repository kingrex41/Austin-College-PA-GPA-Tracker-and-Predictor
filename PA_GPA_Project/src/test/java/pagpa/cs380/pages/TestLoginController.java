package pagpa.cs380.pages;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pagpa.cs380.App;

@ExtendWith(ApplicationExtension.class)
public class TestLoginController {

	@Test
	void test_normal_startup(FxRobot robot) {

		/*
		 * locate the pretend to type in 
		 */
		TextField username = robot.lookup("#usernameField").queryAs(javafx.scene.control.TextField.class);
		assertNotNull(username);
		username.setText("login");
		
		TextField passwd = robot.lookup("#passwordField").queryAs(javafx.scene.control.TextField.class);
		passwd.setText("password");
		
		Button btnLogin = robot.lookup("#loginButton").queryAs(javafx.scene.control.Button.class);
		robot.clickOn(btnLogin);
		
        WaitForAsyncUtils.waitForFxEvents();
        
        // now assert we are at the landing page 
        
        // robot.sleep(5000);
	}
	
	
	/**
	 * Called during testing.... we can mock stuff if needed while
	 * we build the current page with it's controller.   In this example
	 * we are starting from the applications very first scene.
	 * 
	 * @param stage
	 * @throws Exception
	 */
    @Start
    private void start(Stage stage) throws Exception {
    	System.err.println("starting app");
        try {
        	// for testing only,  the app needs to track the current stage
        	App.primaryStage = stage;
        	
            URL fxmlUrl = App.class.getResource("loginpage.fxml");
            if (fxmlUrl == null) {
                System.err.println("Could not find primary.fxml");
                return;
            }
            
            URL cssUrl = App.class.getClassLoader().getResource("style.css");
            if (cssUrl == null) {
                System.err.println("Could not find style.css");
                return;
            }
            
            Scene scene = new Scene(App.loadFXML("loginpage"));
            
            // for testing,  the app needs to track the current scene
            App.scene = scene;
            
            
            scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setTitle("Austin College PA Program");
            stage.setScene(scene);
            stage.show();
            
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
        
       // wait for UI to update
       WaitForAsyncUtils.waitForFxEvents();
    }

}
