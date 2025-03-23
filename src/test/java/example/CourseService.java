package example;

import java.util.HashMap;
import java.util.Map;

public class CourseService {
    private final Map<Long, Course> courses = new HashMap<>();
    
    public CourseService() {
        courses.put(1L, new Course(1L, "Java Programming", "Introduction to Java", 3));
        courses.put(2L, new Course(2L, "Data Structures", "Advanced data structures", 4));
        courses.put(3L, new Course(3L, "Algorithms", "Algorithm design and analysis", 4));
        courses.put(4L, new Course(4L, "Database Systems", "Relational database design", 3));
    }
    
    public Course findById(Long id) {
        return courses.get(id);
    }
} 