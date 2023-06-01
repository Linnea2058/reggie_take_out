import com.itheima.reggie.ReggieApplication;
import com.itheima.reggie.controller.EmployeeController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes= ReggieApplication.class)
public class EmployeeControllerTest {
    @Autowired
    private EmployeeController employeeController;


}
