package pagpa.cs380;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

public class OneDrivePopupController {

    @FXML
    private ProgressIndicator progressIndicator;

    public void handleConnect() {
        progressIndicator.setVisible(true);
        // We can simulate OneDrive connection logic here
       
        System.out.println("Connecting to OneDrive...");
        // Add Microsoft Graph API stuff here 
        // --> example code Desktop.getDesktop().browse(new URI("https://login.microsoftonline.com/common/oauth2/v2.0/authorize?" +
   // "client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=YOUR_REDIRECT_URI&scope=Files.ReadWrite"));

    }

    public void handleCancel() {
        // Close the popup window with this
        Stage stage = (Stage) progressIndicator.getScene().getWindow();
        stage.close();
    }
}
