package practice.restapi.JunitWithPowerMockito;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServer;

public class StudentDAO {
    
   private static List<Student> students;
   private MBeanServer mbeanServer;
    
    public Student getStudent(int rollno) {
        mbeanServer = ManagementFactory.getPlatformMBeanServer();
        students  = creatListOfStudent();
        return findStudentFromList(rollno);
    }
    
    public List<Student> getStudents() {
        students  = creatListOfStudent();
        return students;
    }
    
    public void updateStudent(Student student) {
        student.setMarks(-1);
    }
    
    public void addStudent(Connection con, Student student) {
        students.add(student);
    }
    
    public boolean deleteStudent(int rollno) {
        return true;
    }
    
    private Student findStudentFromList(int rollNo){
        return students.stream().filter(stu->stu.getRollNo() == rollNo).findFirst().get();
    }
    
    private static List<Student> creatListOfStudent() {
        List<Student> students = new ArrayList<>();
        Student student1 = new Student("Stu1", 1 , 500);
        Student student2 = new Student("Stu2", 2 , 200);
        Student student3 = new Student("Stu3", 3 , 300);
        students.add(student1);
        students.add(student2);
        students.add(student3);
        return students;
    }
}
