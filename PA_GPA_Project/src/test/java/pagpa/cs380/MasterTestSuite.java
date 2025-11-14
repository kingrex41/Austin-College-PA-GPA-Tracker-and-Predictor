package pagpa.cs380;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import pagpa.cs380.models.*;
import pagpa.cs380.pages.*;
import pagpa.cs380.utils.*;


@Suite
@SelectClasses({
	LandingPageTest.class,
	LandingUITest.class,
	StudentdashController.class,
	AssignmentTests.class,
	CourseTests.class,
	GPAForecastTests.class,
	RegistrarTests.class,
	StudentTest.class,
	SuccessManagerTests.class,
	AllStudentsViewTest.class,
	TestLoginController.class,
	SuccessManagerTests.class,
	CourseCsvReaderTest.class,
	GradeCsvReaderTest.class,
})

class MasterTestSuite {

	

}
