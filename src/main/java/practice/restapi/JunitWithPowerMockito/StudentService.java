package practice.restapi.JunitWithPowerMockito;

import java.sql.Connection;
import java.util.List;

public class StudentService {
    private StudentDAO studentDAO;
    private StudentProcessing studentProcessing;
    public Student getStudent(int rollno) {
        Student student = getStudentDAO().getStudent(rollno);
        studentProcessing.doProcessing(student);
        Event.logEvent("Student fetched successfuly");
        return student;
        
    }
    
    public List<Student> getStudents() {
        StudentDAO dao = new StudentDAO();
        List<Student> students = dao.getStudents();
        Event.logEvent("List of Students", students);
        return students;
    }
    
    public void updateStudent(Student student) {
        double marks = studentProcessing.calculateStudentMarks();
        student.setMarks(marks);
        getStudentDAO().updateStudent(student);
    }
    
    public void addStudent(Student student) {
        Connection con = ConnectionPool.getInstance().getConnection();
        getStudentDAO().addStudent(con, student);
    }
    
    public boolean deleteStudent(int rollno) {
       return getStudentDAO().deleteStudent(rollno);
    }

    public StudentDAO getStudentDAO() {
        return studentDAO;
    }

    public void setStudentDAO(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    
}

