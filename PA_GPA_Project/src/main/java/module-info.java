module pagpa.cs380 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
	//requires org.junit.jupiter.api;
	//requires org.junit.jupiter.api;
	//requires org.junit.jupiter.params;
//	requires org.junit.jupiter.params; 


    opens pagpa.cs380 to javafx.fxml;
    opens pagpa.cs380.Views to javafx.fxml;
    exports pagpa.cs380;
    exports pagpa.cs380.Models;
    exports pagpa.cs380.Utils;
}
