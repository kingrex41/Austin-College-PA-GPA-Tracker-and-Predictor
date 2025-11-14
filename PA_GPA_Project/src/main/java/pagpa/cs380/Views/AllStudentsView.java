/**
  * The purpose of this class is to: 
  * Create the table seen in the all students UI, and assign Student object data to columns
  * Add a check box to the table to select students for report generation eventually
  */

package pagpa.cs380.Views;   

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.scene.control.TableRow;
import pagpa.cs380.App;
import pagpa.cs380.PaController;
import pagpa.cs380.StudentdashController;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.SuccessStanding;
import pagpa.cs380.Utils.Registrar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;

import java.time.LocalDate;
import java.time.Month;

public class AllStudentsView extends StudentdashController implements Initializable {

	private Registrar registrar = Registrar.getCurrent();
	
/*
 * This part of the code is declaring instance variables. There is the main table, all the columns within it, buttons
 * commboboxes, studentlists, and a string set to null
 * 
 * all set to private
 */

    @FXML private TableView<Student> studentTable;
    
    @FXML private TableColumn<Student, Integer> acidColumn;
    
    @FXML private TableColumn<Student, String> firstNameColumn;
    
    @FXML private TableColumn<Student, String> lastNameColumn;
    
    @FXML private TableColumn<Student, Double> gpaColumn;
    
    @FXML private TableColumn<Student, Double> liveGpaColumn;
    
    @FXML private TableColumn<Student, Double> termGpaColumn;
    
    @FXML private TableColumn<Student, String> riskLevelColumn;
    
    @FXML private TableColumn<Student, String> advisorColumn;
    
    @FXML private TableColumn<Student, String> phoneColumn;
    
    @FXML private TableColumn<Student, String> cohortColumn;
    
    @FXML private Button exportToExcelButton;
    
    @FXML private TextField searchField;
    
    @FXML private ComboBox<String> cohortComboBox;
    
    @FXML private ComboBox<String> riskComboBox;
    
    @FXML private ComboBox<String> advisorComboBox;
    
 
    private static String presetRiskLevel = null; //set to null in case the pie chart is not clicked and all students is clicked instead (from landing page)
    
    private static String presetCohortFilter = null; // For pre-filtering by cohort when coming from landing page
    
    private FilteredList<Student> filteredList;
    
    private SortedList<Student> sortedList;
    
    private LocalDate date;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DEBUG: AllStudentsView initialize called");
    	
/*	
 * This portion of the code sets up the table, studentTable
 */
    	
        // Set up columns and take their information from Student
        acidColumn.setCellValueFactory(new PropertyValueFactory<>("acid"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        // Use registrar to calculate gpa for student
        gpaColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            double gpa = registrar.calcGpaForStudent(student, false);
            return new javafx.beans.property.SimpleDoubleProperty(gpa).asObject();
        });

        liveGpaColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            double liveGpa = registrar.calcGpaForStudent(student, true);
            return new javafx.beans.property.SimpleDoubleProperty(liveGpa).asObject();
        });
        
        //adds live term GPA using switch and local date to determine which term to show
        termGpaColumn.setCellValueFactory(cellData -> {
        	Student student = cellData.getValue();
     
        	String term = registrar.getCurrentTerm();
        	double tGpa = registrar.calcTermGpa(student, term, false);  // not including forecasted grades
        	
        	return new javafx.beans.property.SimpleDoubleProperty(tGpa).asObject();
        });

        // Format GPA columns to show 2 decimal places
        gpaColumn.setCellFactory(column -> {
            return new TableCell<Student, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            };
        });

        liveGpaColumn.setCellFactory(column -> {
            return new TableCell<Student, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            };
        });
        
        termGpaColumn.setCellFactory(column -> {
            return new TableCell<Student, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            };
        });

        riskLevelColumn.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        advisorColumn.setCellValueFactory(new PropertyValueFactory<>("advisor"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        cohortColumn.setCellValueFactory(new PropertyValueFactory<>("cohort"));
        
        // Add column for checkboxes
        TableColumn<Student, Boolean> selectColumn = new TableColumn<>("Select");
        selectColumn.setPrefWidth(75);
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true); //need to be set to editable to allow for the check box to be clicked
        studentTable.setEditable(true);
        studentTable.getColumns().add(0, selectColumn); //adds to 0 so it can add in as the first column
        
        //adds checkbox to the table header for selecting all
        CheckBox master = new CheckBox();
        master.setCursor(Cursor.HAND); 
        selectColumn.setGraphic(master); //adds checkbox to the header

        //works when checkbox is clicked on
        master.setOnAction(e -> {
            boolean selectAll = master.isSelected();   // if true, check all rows. If untrue, unselect all
            studentTable.getItems().forEach(stu -> stu.setSelected(selectAll)); //sets them based on what is in the list 
        });
        


/*
 * This portion of the code creates comboboxes for selecting cohort, risk, and advisor for filtering the students list
 * All is added as an option to all comboboxes
 */
        // Initialize the student list and filtered list
        ObservableList<Student> studentList = FXCollections.observableArrayList(registrar.getStudents());
        filteredList = new FilteredList<>(studentList);
        sortedList = new SortedList<>(filteredList);
        studentTable.setItems(sortedList);

        // Populate cohort ComboBox
        ObservableList<String> cohorts = FXCollections.observableArrayList("All Cohorts");
        for (Student student : studentList) {
            String cohort = String.valueOf(student.getCohort());
            if (!cohorts.contains(cohort)) {
                cohorts.add(cohort);
            }
        }
        cohortComboBox.setItems(cohorts);
        System.out.println("DEBUG: Available cohorts: " + cohorts);

        // Populate risk level ComboBox
        ObservableList<String> riskLevels = FXCollections.observableArrayList("All", "Meets Expectations", "Close Monitoring", "Academic Probation", "Dismissed");
        riskComboBox.setItems(riskLevels);

        // Populate advisor ComboBox
        ObservableList<String> advisors = FXCollections.observableArrayList("All");
        for (Student student : studentList) {
            String advisor = student.getAdvisor();
            if (advisor != null && !advisors.contains(advisor)) {
                advisors.add(advisor);
            }
        }
        advisorComboBox.setItems(advisors);

        // Set default values
        cohortComboBox.setValue("All Cohorts");
        riskComboBox.setValue("All");
        advisorComboBox.setValue("All");

        // Handle preset cohort filter from landing page
        if (presetCohortFilter != null) {
            System.out.println("DEBUG: Found preset cohort filter: " + presetCohortFilter);
            cohortComboBox.setValue(presetCohortFilter);
            System.out.println("DEBUG: Set cohort ComboBox value to: " + cohortComboBox.getValue());
            presetCohortFilter = null; // Reset after applying
        }

        // Handle preset risk level from pie chart
        if (presetRiskLevel != null && !presetRiskLevel.equalsIgnoreCase("All")) {
            riskComboBox.setValue(presetRiskLevel);
            presetRiskLevel = null;
        }

        // Add listeners to all required portions of the UI that updated filteredList using updateFilters()
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        cohortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: Cohort ComboBox value changed from " + oldVal + " to " + newVal);
            updateFilters();
        });
        riskComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        advisorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());

        // Initial filter update
        updateFilters();
        
/*
 * This portion of the code ensures that rows can be clicked and they switch us to student dash board
 */
        
        // Add click handler for rows
        studentTable.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    Student student = row.getItem();
                    try {
                        App.switchScene("studentdash2");
                        StudentdashController sc = (StudentdashController) App.getController();
                        sc.setStudent(student);  // now the controller knows which student to show
                        sc.finishInit();
                        
                    } catch (IOException e) {
                        // Handle exception appropriately
                    }
                }
            });
            return row;
        });
    }

/*
 * This portion of the code switches us back to the main menu if clicked 
 */
    
    @FXML
    private void switchToMainMenu(ActionEvent event) throws IOException {
        App.switchScene("primary");
    }
    

/*
 * This method is made to set riskLevel to the presetRisk level
 * and preset risk level is set based on which slice of pie is clicked
 */
    
    public static void setPresetRiskLevel(String riskLevel) {
        presetRiskLevel = riskLevel;
    }
    
    public static void setPresetCohortFilter(String cohort) {
        System.out.println("DEBUG: setPresetCohortFilter called with cohort: " + cohort);
        presetCohortFilter = cohort;
    }
    
 /*
  * This method updates the filters when listener is called on
  * it ensures that all filters work within the cohort filter   
  */

    private void updateFilters() {
        System.out.println("DEBUG: updateFilters called");
        System.out.println("DEBUG: Current cohort value: " + cohortComboBox.getValue());
        
        filteredList.setPredicate(student -> {
            // gets value of the combo boxes / search field to read it
            String cohort = cohortComboBox.getValue();
            String risk = riskComboBox.getValue();
            String advisor = advisorComboBox.getValue();
            String search = searchField.getText();

            System.out.println("DEBUG: Filtering student " + student.getFirstName() + " " + student.getLastName());
            System.out.println("DEBUG: Student cohort: " + student.getCohort() + ", Filter cohort: " + cohort);

            // These work as follows:
            // if the user selection is not null, and does not equal "all" (in these two cases, no filtering is needed)
            // and the selected cohort, risk, name, or advisor, is not equal to a students name, then we return false
            // returning false means that student is not added to the filtered list
            // on the flip side, if the selection is null/ set to all, and the selection does match with a students get value
            // the student is true and added to the filteredList which is then displayed 
            // predicate is set for all true students
            
            if (cohort != null && !cohort.equals("All Cohorts") && !cohort.equals(String.valueOf(student.getCohort()))){
                System.out.println("DEBUG: Filtering out student due to cohort mismatch");
                return false;
            }
            if (risk != null && !risk.equals("All") && !risk.equals(student.getRiskLevel())){
                return false;
            }
            if (advisor != null && !advisor.equals("All") && !advisor.equals(student.getAdvisor())){
                return false;
            }
            if (search != null && !search.isEmpty() &&
                    !(student.getFirstName().toLowerCase().contains(search.toLowerCase()) ||
                      student.getLastName().toLowerCase().contains(search.toLowerCase()))) {
                return false;
            }

            System.out.println("DEBUG: Student passed all filters");
            return true;
        });
    }
    
/*
 * This portion of the code created an ActionEvent for the Export to Excel button
 * When clicked, an excel sheet is made for the data of the selected students
 */
    
    @FXML
    private void handleExport(ActionEvent event) {
    	
        ObservableList<Student> selectedStuds =
                studentTable.getItems().filtered(Student::isSelected);
        
        /*
         * This makes a popup occur is the Export to Excel button is clicked but no students were selected
         */

        if (selectedStuds.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                      "No students were selected.\n"
                      + "Select one or more Students first.")
                    .showAndWait();
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx")); //chooses the extension of the file, they want an export to excel which is 
        chooser.setInitialFileName("Selected_Students.xlsx"); //sets the preset name to Sleected_Students but they can actually name is whatever they want

        File dest = chooser.showSaveDialog(studentTable.getScene().getWindow());
        if (dest == null) {
        	System.out.println("Cancelled request"); //checks to make sure this line is being called/ reached
        	return; //this is if cancel is clicked by the user 
        }

        //builds workbook
        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(dest)) {

            Sheet sheet = wb.createSheet("Student GPA and Info Data"); //creates new sheet in excel workbook, name can be edited based on PA program request
            
            //this makes the 2 decimal format for the data that is GPA to go out by two digits
            //this might not be needed at all since GPA is cut down to two digits in other area
            DataFormat fmt = wb.createDataFormat();
            CellStyle numberStyle = wb.createCellStyle();
            numberStyle.setDataFormat(fmt.getFormat("#,##0.00"));

            //Header row, titles are taken from TableView for consistency
            Row header = sheet.createRow(0);
            //go through each of the column names and get that text and assign it to the headers
            for (int c = 1; c < studentTable.getColumns().size(); c++) {
            
                TableColumn<?,?> col = studentTable.getColumns().get((c));
                header.createCell(c).setCellValue(col.getText());
            	
            }

            //Data rows now for each selected student
            int r = 1; //row 0 is taken by header
            for (Student stud : selectedStuds) {
                Row row = sheet.createRow(r++);
                int c = 1;

                //gets student information for each student in the list
                row.createCell(c++).setCellValue(stud.getAcid());
                row.createCell(c++).setCellValue(stud.getFirstName());
                row.createCell(c++).setCellValue(stud.getLastName());

                //CUMULATIVE GPA
                Cell gCell = row.createCell(c++);
                gCell.setCellValue(registrar.calcGpaForStudent(stud, false));
                gCell.setCellStyle(numberStyle);

                /*
                 * UPDATE WITH UPDATED GPA CALC METHODS ONCE DONE
                 */
                //THIS WILL BE LIVE GPA
                //THIS WILL BE UPDATED TO LIVE GPA
                Cell lgCell = row.createCell(c++);
                lgCell.setCellValue(registrar.calcGpaForStudent(stud, false));
                lgCell.setCellStyle(numberStyle);

                //adding rest of the info for students, can remove anything they may not want
                row.createCell(c++).setCellValue(stud.getRiskLevel());
                row.createCell(c++).setCellValue(stud.getAdvisor());
                row.createCell(c++).setCellValue(stud.getPhone());
                row.createCell(c++).setCellValue(stud.getCohort());
            }
            
            //to make up fore deleting the select column, this shifts all columns down one
            int lastCol = sheet.getRow(0).getLastCellNum() - 1; 
            sheet.shiftColumns(1,lastCol,-1); 

            //Using switch statments, each row in the sheet gets a size
            //The way this works is: go through all the columns with a for loop and 
            //give them a number c
            //based on what the c value is, they get a different size
            //the number mult by 256 is how many chars wide it is
            for (int c = 0; c < studentTable.getColumns().size(); c++) {
                int width;
                switch (c) {
                    case 0 -> width = 10 * 256;  // ACID
                    case 1, 2 -> width = 20 * 256; // Names
                    case 3, 4 -> width = 6 * 256;  // GPAs
                    case 5 -> width = 10 * 256;    // Risk
                    case 6 -> width = 22 * 256;    // Advisor
                    case 7 -> width = 14 * 256;    // Phone
                    case 8 -> width = 8 * 256;     // Cohort
                    default -> width = 10 * 256;
                }
                sheet.setColumnWidth(c, width);
            }

            
            wb.write(out); //saves

        } catch (IOException ex) {
            ex.printStackTrace();     // replace with dialog / logger in real app
        }
    }
    
}




