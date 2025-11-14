package pagpa.cs380;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import pagpa.cs380.Utils.Registrar;

/**
 * JavaFX App
 */

public class App extends Application { 

    private static final double INITIAL_SCREEN_PERCENT = 0.90;
	
    public static Scene scene;
    public static Object controller;
    public static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws IOException {
        try {
        	Registrar.getCurrent();  
        	
            primaryStage = stage;
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
            
            scene = new Scene(loadFXML("loginpage"));
            scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setTitle("Austin College PA Program");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    /**
     * wrapper function for programmers convenience.   When the page is switched
     * with this method, we do not expand the window size....typical of all other
     * page switches except switching from login window to landing page.
     * 
     * @param fxml
     * @throws IOException
     */
    public static void setRoot(String fxml) throws IOException {
    	setRoot(fxml,false);
    }
    
    /**
     * Allows the caller to reset the stage to contain a different GUI.   Used
     * to switch from one "page" to another.   If <code>expand==true</code> then
     * expands and centers the stage on the user's primary screen to a percentage
     * controlled via INITIAL_SCREEN_PERCENT.
     * 
     * @param fxml
     * @param expand
     * @throws IOException
     */
    public static void setRoot(String fxml, boolean expand) throws IOException {
        System.out.println("Switching to " + fxml);
        scene.setRoot(loadFXML(fxml));
        
        /*
         * Now resize stage window to be 80% of user's screen.
         */
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double width = screen.getWidth()*INITIAL_SCREEN_PERCENT;
        double height = screen.getHeight()*INITIAL_SCREEN_PERCENT;
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.setX(screen.getMinX()+(1.0-INITIAL_SCREEN_PERCENT)/2*width);
        primaryStage.setY(screen.getMinY()+(1.0-INITIAL_SCREEN_PERCENT)/2*height);
        
    }

    public static Parent loadFXML(String fxml) throws IOException {
        System.out.println("Loading FXML: " + fxml);
        URL fxmlUrl = App.class.getResource(fxml + ".fxml");
        if (fxmlUrl == null) {
            throw new IOException("Could not find " + fxml + ".fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Parent result = fxmlLoader.load();
        System.err.println("after load");
        controller = fxmlLoader.getController();
        
        return result; 
    }
    

    /**
     * Use this to rebuild the scene by swapping out the scene graph.  As
     * a side effect the App.controller handle will be set to hold the current
     * scenes controller.
     * 
     * @param fxml
     * @throws IOException
     */
    public static void switchScene(String fxml) throws IOException {
        System.out.println("Switching scene to: " + fxml);
        try {
            URL fxmlUrl = App.class.getResource(fxml + ".fxml");
            if (fxmlUrl == null) {
                System.err.println("Could not find " + fxml + ".fxml");
                return;
            }

            scene.setRoot(loadFXML(fxml));
            
            System.err.println("switch complete");
            
        } catch (Exception e) {
            System.err.println("Error switching scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Scene getScene() {
    	return scene;
    }
    
    public static Object getController() {
    	return controller;
    }
    
    public static void main(String[] args) {
        
        launch();
        
    }
}