package practice.restapi.JunitWithPowerMockito;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

/**
 * java.lang.LinkageError: loader constraint violation: when resolving method
 * "java.lang.management.ManagementFactory.getPlatformMBeanServer()Ljavax/management/MBeanServer;"
 * the class loader (instance of org/powermock/core/classloader/MockClassLoader) of the current class,
 * practice/restapi/JunitWithPowerMockito/StudentDAO, and the class loader (instance of <bootloader>)
 * for the method's defining class, java/lang/management/ManagementFactory, 
 * have different Class objects for the type javax/management/MBeanServer used in the signature.
 * 
 *---- We can defer loading of certain packages to system class loader using @PowerMockIgnore annotation.
 */
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
/** 
 * This annotation tells PowerMock to prepare certain classes for testing.
 * Classes needed to be defined using this annotation are typically those that
 * needs to be byte-code manipulated. This includes final classes, classes with
 * final, private, static or native methods that should be mocked and also
 * classes that should be return a mock object upon instantiation.
 */
@PrepareForTest({StudentDAO.class,
                 StudentService.class,
                 StudentProcessing.class,
                 Event.class,
                 ConnectionPool.class})
public class StudentServiceTest {

    
    @InjectMocks
    StudentService studentService;

    @Mock
    StudentDAO studentDAO;
    
    @Mock
    StudentProcessing studentProcessing;
    
    @Test
    public void callRealMethod_succeed() {
        PowerMockito.when(studentDAO.deleteStudent(1)).thenCallRealMethod();
        boolean returnedValue = studentService.deleteStudent(1);
        Assert.assertTrue(returnedValue);
    }
    
    @Test
    public void matchersAndWhenThenReturn_succeed() {
        Student student = new Student("Stu1", 1, 500);
        PowerMockito.when(studentDAO.getStudent(Matchers.anyInt())).thenReturn(student);
        Student returnedStudent = studentService.getStudent(5);
        Assert.assertTrue(student == returnedStudent);
    }
    
    @Test
    public void matchersAndDoReturnWhen_succeed() {
        Student student = new Student("Stu1", 1, 500);
        PowerMockito.doReturn(student).when(studentDAO).getStudent(Matchers.anyInt());
        Student returnedStudent = studentService.getStudent(5);
        Assert.assertTrue(student == returnedStudent);
    }
    
    @Test
    public void doVerifyInstanceMethod_succeed() {
        Student student = new Student("Stu1", 1, 500);
        PowerMockito.when(studentDAO.getStudent(Matchers.anyInt())).thenReturn(student);
        Student returnedStudent = studentService.getStudent(5);
        Mockito.verify(studentProcessing, Mockito.times(1)).doProcessing(student);
        Assert.assertTrue(student == returnedStudent);
    }
    
    @Test
    public void doVerifyStaticMethod_succeed() {
        PowerMockito.mockStatic(Event.class);
        Student student = new Student("Stu1", 1, 500);
        PowerMockito.when(studentDAO.getStudent(Matchers.anyInt())).thenReturn(student);
        Student returnedStudent = studentService.getStudent(5);
        PowerMockito.verifyStatic(Event.class, Mockito.times(1));
        Event.logEvent(Matchers.anyString());
        Assert.assertTrue(student == returnedStudent);
    }
    
    @Test
    public void doVerifyArugmentCaptor_succeed() {
        Student student = new Student("Stu1", 1, 500);
        PowerMockito.when(studentDAO.getStudent(Matchers.anyInt())).thenReturn(student);
        studentService.getStudent(5);
        ArgumentCaptor<Student> argument = ArgumentCaptor.forClass(Student.class);
        Mockito.verify(studentProcessing, Mockito.times(1)).doProcessing(argument.capture());
        Student capturedStudent = argument.getValue();
        Assert.assertTrue(student == capturedStudent);
    }
    
    @Test
    public void mockPrivateMethod_succeed() throws Exception {
        Student student = new Student("Stu1", 1, 500);
        StudentService studentServiceSpy = PowerMockito.spy(this.studentService);
        StudentDAO studentDAO = new StudentDAO();
        StudentDAO studentDAOSpy = PowerMockito.spy(studentDAO);
        /*
         * when we use spies we always should use the do* methods as against then* methods.
         * For e.g. the below code uses doReturn() instead of thenReturn()
         */
        PowerMockito.doReturn(student).when(studentDAOSpy, "findStudentFromList", 20);
        PowerMockito.doReturn(studentDAOSpy).when(studentServiceSpy, "getStudentDAO");
        Student returnedStudent = studentServiceSpy.getStudent(20);
        Assert.assertTrue(student == returnedStudent);
    }
    
    @Test
    public void mockStaticPrivateMethod_succeed() throws Exception {
        PowerMockito.mockStatic(StudentDAO.class);
        StudentDAO studentDAO = new StudentDAO();
        studentService.setStudentDAO(studentDAO);
        Student student = new Student("Stu1", 20, 500);
        List<Student> students = new ArrayList<>();
        students.add(student);
        PowerMockito.doReturn(students).when(StudentDAO.class, "creatListOfStudent");
        Student returnedStudent = studentService.getStudent(20);
        Assert.assertTrue(student == returnedStudent);
    }
    
    /**
     * For mocking classes/method marked final we need to specify the class in 
     * @org.powermock.core.classloader.annotations.PrepareForTest annotation. 
     * Additionally we need to run the test using org.powermock.modules.junit4.PowerMockRunner
     * 
     * @throws Exception
     */
    @Test
    public void mockFinalMethodNClass_succeed() throws Exception {
        Student student = new Student("Stu1", 20, 500);
        PowerMockito.when(studentProcessing.calculateStudentMarks()).thenReturn(588d);
        studentService.updateStudent(student);
        Assert.assertTrue(student.getMarks() == 588);
    }
    
    /**
     * We can invoke and test behavior of private methods using org.powermock.reflect.Whitebox.
     * This is used without mocking.
     * 
     * @throws Exception
     */
    @Test
    public void invokePrivateStaticMethodNGetResult_succeed() throws Exception {
        List<Student> studentList = Whitebox.invokeMethod(StudentDAO.class, "creatListOfStudent");
        Assert.assertTrue(studentList.size() == 3);
    }
    
    @Test
    public void setPrivateFieldNinvokePrivateMethodNGetResult_succeed() throws Exception {
        StudentDAO studentDAO = new StudentDAO();
        Field field = PowerMockito.field(StudentDAO.class, "students");
        List<Student> studentList = Whitebox.invokeMethod(StudentDAO.class, "creatListOfStudent");
        field.set(studentDAO, studentList);
        Student student = Whitebox.invokeMethod(studentDAO, "findStudentFromList", 1);
        Assert.assertTrue(student.getRollNo() == 1);
    }
    
    @Test
    public void spyPrivateMethodNVerifyCallHappened_succeed() throws Exception {
        StudentService studentServiceSpy = PowerMockito.spy(this.studentService);
        StudentDAO studentDAO = new StudentDAO();
        StudentDAO studentDAOSpy = PowerMockito.spy(studentDAO);
        /*
         * when we use spies we always should use the do* methods as against then* methods.
         * For e.g. the below code uses doReturn() instead of thenReturn()
         */
        PowerMockito.doReturn(null).when(studentDAOSpy, "findStudentFromList", 20);
        PowerMockito.doReturn(studentDAOSpy).when(studentServiceSpy, "getStudentDAO");
        Student returnedStudent = studentServiceSpy.getStudent(20);
        Assert.assertNull(returnedStudent);
        PowerMockito.verifyPrivate(studentDAOSpy, Mockito.times(1)).invoke("findStudentFromList", 20);
    }
    
    /**
     * For non-void methods use PowerMockito.thenThrow().
     * 
     * @throws RuntimeException
     */
    @Test
    public void throwExceptionOnNonVoidMethodCall_succeed() throws RuntimeException {
        PowerMockito.when(studentDAO.getStudent(Matchers.anyInt())).thenThrow(new RuntimeException());
        try {
            studentService.getStudent(5);
            Assert.fail("expected exception to be thrown");
        } catch(RuntimeException e) { }
    }
    
    /**
     * We can use PowerMockito.doThrow() when we stub a void method with exception.
     * 
     * @throws RuntimeException
     */
    @Test
    public void throwExceptionOnVoidMethodCall_succeed() throws RuntimeException {
        Student student = new Student("Stu1", 1, 500);
        PowerMockito.doThrow(new RuntimeException()).when(studentDAO).updateStudent(Matchers.any(Student.class));
        try {
            studentService.updateStudent(student);
            Assert.fail("expected exception to be thrown");
        } catch(RuntimeException e) { }
    }
    
    @Test
    public void doNothingMethodCall_succeed() throws RuntimeException {
        Student student = new Student("Stu1", 1, 500);
        StudentDAO studentDAO = PowerMockito.spy(new StudentDAO());
        studentService.setStudentDAO(studentDAO);
        PowerMockito.when(studentProcessing.calculateStudentMarks()).thenReturn(588d);
        studentService.updateStudent(student);
        Assert.assertTrue(student.getMarks() == -1);
        // After doNothing its value not get reset to -1.
        PowerMockito.doNothing().when(studentDAO).updateStudent(Matchers.any(Student.class));
        studentService.updateStudent(student);
        Assert.assertTrue(student.getMarks() == 588);
    }

    /**
     * Use PowerMockito.doNothing() whereever possible in favor of PowerMockito.suppress()
     * 
     * @throws RuntimeException
     */
    @Test
    public void doSuppressOnMethodCall_succeed() throws RuntimeException {
        Student student = new Student("Stu1", 20, 500);
        PowerMockito.suppress(PowerMockito.method(StudentProcessing.class, "calculateStudentMarks"));
        studentService.updateStudent(student);
        Assert.assertTrue(student.getMarks() == 0);
    }
    
    @Test
    public void thenAnswer_succeed() throws RuntimeException {
        Student student = new Student("Stu1", 20, 500);
        PowerMockito.when(studentProcessing.calculateStudentMarks()).thenAnswer(new Answer<Double>(){
            // declare variable if needed.
            int i = 20;
            public Double answer(InvocationOnMock invocationOnMock){
                // get argument if needed.
                Object[] args = invocationOnMock.getArguments();
                return 23.33d;
            }
        });
        studentService.updateStudent(student);
        Assert.assertTrue(student.getMarks() == 23.33);
    }
    
    @Test
    public void mockSingletonObject_succeed() throws RuntimeException {
        PowerMockito.mockStatic(ConnectionPool.class);
        ConnectionPool pool = PowerMockito.mock(ConnectionPool.class);
        PowerMockito.when(ConnectionPool.getInstance()).thenReturn(pool);
        Connection conn = PowerMockito.mock(Connection.class);
        PowerMockito.when(ConnectionPool.getInstance().getConnection()).thenReturn(conn);
        Student student = new Student("Stu1", 20, 500);
        PowerMockito.suppress(PowerMockito.method(StudentDAO.class, "addStudent", Connection.class, Student.class));
        studentService.addStudent(student);
        Mockito.verify(studentDAO, Mockito.times(1)).addStudent(conn, student);
    }
    
    @Test
    public void mockConstructor_succeed() throws Exception {
       PowerMockito.whenNew(StudentDAO.class).withNoArguments().thenReturn(studentDAO);
       PowerMockito.when(studentDAO.getStudents()).thenReturn(Arrays.asList(new Student("Stu1", 29, 500)));
       List<Student> students = studentService.getStudents();
       Assert.assertTrue(students.size() == 1);
       Assert.assertTrue(students.get(0).getRollNo() == 29);
    }
    
    /**
     * Sometimes the invoked real methods creates a collection internally and adds objects to it.
     * The collection is then used in a stubbed/mocked method call.
     * 
     * @throws Exception
     */
    @Test
    public void customArgumentMatcher_succeed() throws Exception {
       PowerMockito.mockStatic(Event.class);
       Student student1 = new Student("Stu1", 1 , 500);
        ArgumentMatcher<List<Student>> eventArgumentMatcher = new ArgumentMatcher<List<Student>>() {
            @Override
            public boolean matches(Object argument) {
                List<Student> students = (List<Student>) argument;
                return students.size() == 3 && students.contains(student1);
            }
        };
       studentService.getStudents();
       PowerMockito.verifyStatic(Event.class, Mockito.times(1));
       Event.logEvent(Matchers.anyString(), Matchers.argThat(eventArgumentMatcher));
       
      
    }

}
