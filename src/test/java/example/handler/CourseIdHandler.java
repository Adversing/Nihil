package example.handler;

import me.adversing.nihil.intf.IPropertyHandler;
import me.adversing.nihil.annotation.Dependency;
import example.Course;
import example.CourseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CourseIdHandler implements IPropertyHandler<List<Long>> {
    
    @Dependency
    private CourseService courseService;
    
    @Override
    public Object process(List<Long> courseIds) {
        if (courseIds == null) {
            return new ArrayList<>();
        }
        
        List<Course> courses = courseIds.stream()
            .map(id -> courseService.findById(id))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            
        System.out.println("CourseIdHandler: Converting " + courseIds.size() + " ID in " + courses.size() + " courses");
        
        return courses;
    }
} 