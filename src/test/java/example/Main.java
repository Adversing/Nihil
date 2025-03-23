package example;

import java.util.List;
import java.util.Objects;

import me.adversing.nihil.Nihil;
import me.adversing.nihil.config.NihilConfig;

public class Main {
    public static void main(String[] args) {
        // Create a student entity
        Student student = new Student(1L, "John", "Doe", "john.doe@example.com");
        
        // Create a course service for dependency injection
        CourseService courseService = new CourseService();
        
        System.out.println("Original student: " + student);
        
        // Create a DTO with partial updates
        StudentDTO updateDTO = new StudentDTO();
        updateDTO.setEmail("john.updated@example.com");
        updateDTO.setCourseIds(List.of(1L, 3L));
        
        // Create an address
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet("123 University Ave");
        addressDTO.setCity("College Town");
        updateDTO.setAddress(addressDTO);
        
        // Standard configuration with reflection
        System.out.println("\n=== Using Standard Update ===");
        Nihil updater = Nihil.create();
        updater.forTarget(student)
               .withDependency(CourseService.class, courseService)
               .update(updateDTO);

        System.out.println("Updated student: " + student);
        
        // Reset student for next demo
        student = new Student(1L, "John", "Doe", "john.doe@example.com");
        
        // Custom configuration demo
        System.out.println("\n=== Using Custom Configuration ===");
        
        // Reset student for a clean slate
        student = new Student(1L, "John", "Doe", "john.doe@example.com");
        
        // Create a new updater with a different approach
        NihilConfig config = NihilConfig.builder()
                .withAccessStrategy(NihilConfig.AccessStrategy.AUTO)
                .withIgnoreNull(true)  // Ignore null values
                .build();
        
        System.out.println("Custom Configuration: " + config);
        
        // Create a custom DTO with only the email in uppercase
        StudentDTO customDTO = new StudentDTO();
        customDTO.setEmail(updateDTO.getEmail().toUpperCase());
        customDTO.setAddress(updateDTO.getAddress());
        customDTO.setCourseIds(updateDTO.getCourseIds());
        
        Nihil customUpdater = Nihil.create(config);
        customUpdater.forTarget(student)
                    .withDependency(CourseService.class, courseService)
                    .update(customDTO);
        
        System.out.println("Updated student with custom config: " + student);
        
        // Compare with traditional approach
        System.out.println("\n=== Traditional Update vs Nihil ===");
        Student student1 = new Student(1L, "John", "Doe", "john.doe@example.com");
        Student student2 = new Student(1L, "John", "Doe", "john.doe@example.com");
        
        // Traditional update with null checks
        updateTraditional(student1, updateDTO, courseService);
        
        // Nihil approach
        System.out.println("\nUsing Nihil with Standard Configuration:");
        Nihil.create()
                   .forTarget(student2)
                   .withDependency(CourseService.class, courseService)
                   .update(updateDTO);
        
        System.out.println("Traditional update result: " + student1);
        System.out.println("Nihil result: " + student2);
    }
    
    /**
     * Traditional update method with null checks
     */
    private static void updateTraditional(Student student, StudentDTO dto, CourseService courseService) {
        if (dto.getFirstName() != null) {
            student.setFirstName(dto.getFirstName());
        }
        
        if (dto.getLastName() != null) {
            student.setLastName(dto.getLastName());
        }
        
        if (dto.getEmail() != null) {
            student.setEmail(dto.getEmail());
        }
        
        if (dto.getDateOfBirth() != null) {
            student.setDateOfBirth(dto.getDateOfBirth());
        }
        
        if (dto.getAddress() != null) {
            if (student.getAddress() == null) {
                student.setAddress(new Address());
            }
            
            AddressDTO addressDTO = dto.getAddress();
            if (addressDTO.getStreet() != null) {
                student.getAddress().setStreet(addressDTO.getStreet());
            }
            
            if (addressDTO.getCity() != null) {
                student.getAddress().setCity(addressDTO.getCity());
            }
            
            if (addressDTO.getState() != null) {
                student.getAddress().setState(addressDTO.getState());
            }
            
            if (addressDTO.getZipCode() != null) {
                student.getAddress().setZipCode(addressDTO.getZipCode());
            }
        }
        
        if (dto.getCourseIds() != null) {
            student.setEnrolledCourses(dto.getCourseIds().stream()
                .map(courseService::findById)
                .filter(Objects::nonNull)
                .toList());
        }
    }
}
