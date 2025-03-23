package example;

import java.time.LocalDate;
import java.util.List;

import me.adversing.nihil.annotation.UpdateProperty;
import example.handler.AddressDTOHandler;
import example.handler.CourseIdHandler;

public class StudentDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    
    @UpdateProperty(handler = AddressDTOHandler.class)
    private AddressDTO address;
    
    @UpdateProperty(handler = CourseIdHandler.class, targetProperty = "enrolledCourses")
    private List<Long> courseIds;
    
    public StudentDTO() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public AddressDTO getAddress() { return address; }
    public void setAddress(AddressDTO address) { this.address = address; }
    
    public List<Long> getCourseIds() { return courseIds; }
    public void setCourseIds(List<Long> courseIds) { this.courseIds = courseIds; }
} 