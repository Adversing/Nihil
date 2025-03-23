package example;

public class Course {
    private Long id;
    private String name;
    private String description;
    private Integer credits;
    
    public Course() {}
    
    public Course(Long id, String name, String description, Integer credits) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.credits = credits;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    
    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", credits=" + credits +
                '}';
    }
} 