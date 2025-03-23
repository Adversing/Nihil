# Nihil

**Nihil** is a lightweight, framework-agnostic Java library designed to eliminate repetitive null-checking boilerplate code when updating object properties. Say goodbye to endless `if (value != null) { target.setValue(value); }` statements!

## Features

- 🔍 **Zero null checks** - Automatically skip null values when updating objects
- 🔌 **Framework agnostic** - Works in any Java application, no Spring or Jakarta dependencies
- 🧩 **Polymorphic design** - Extensible architecture with plugin support
- 🛠️ **Multiple access strategies** - Works with setters, direct field access, or both
- 🧬 **Type safety** - Maintains Java's strong typing throughout
- ⚡ **Performance optimized** - Method/field caching for minimal overhead
- 🔄 **Transformation support** - Apply transformations to property values
- 🎯 **Custom mapping** - Map source properties to differently named target properties
- 🧪 **Lombok compatible** - Works with Lombok-generated classes

## Quick Start

### Basic Usage

```java
import me.adversing.nihil.Nihil;

// Source DTO with some null fields
UserDTO userDTO = new UserDTO();
userDTO.setEmail("john@example.com");
userDTO.setFirstName("John");
// lastName is null
// phoneNumber is null

// Target entity
User user = userRepository.findById(1L);

// Update user with non-null values from DTO
Nihil.create().update(user, userDTO);
// Only email and firstName will be updated!
```

### With Custom Configuration

```java
import me.adversing.nihil.Nihil;
import me.adversing.nihil.config.NihilConfig;

// Create custom configuration
NihilConfig config = NihilConfig.builder()
    .withAccessStrategy(NihilConfig.AccessStrategy.FIELD) // Direct field access
    .withIgnoreNull(true)                                 // Skip null values
    .ignoreProperty("createdAt")                          // Skip specific properties
    .build();

// Create updater with custom config
Nihil updater = Nihil.create(config);
updater.update(user, userDTO);
```

### With Dependencies and Transformations

```java
import me.adversing.nihil.Nihil;

// Update with complex dependencies and transformations
Nihil.create()
    .forTarget(user)
    .withDependency(DepartmentRepository.class, departmentRepo)
    .withMapping("deptId", "department") // Map deptId property to department
    .withTransformer("email", email -> email.toLowerCase()) // Transform email to lowercase
    .update(userDTO);
```

## Comparison with Traditional Approach

### Before (Traditional Approach)

```java
public User updateUser(User user, UserDTO userDTO) {
    if (userDTO.getFirstName() != null) {
        user.setFirstName(userDTO.getFirstName());
    }
    
    if (userDTO.getLastName() != null) {
        user.setLastName(userDTO.getLastName());
    }
    
    if (userDTO.getEmail() != null) {
        user.setEmail(userDTO.getEmail());
    }
    
    if (userDTO.getPhoneNumber() != null) {
        user.setPhoneNumber(userDTO.getPhoneNumber());
    }
    
    if (userDTO.getAddress() != null) {
        if (user.getAddress() == null) {
            user.setAddress(new Address());
        }
        
        if (userDTO.getAddress().getStreet() != null) {
            user.getAddress().setStreet(userDTO.getAddress().getStreet());
        }
        
        // ... more address fields
    }
    
    // ... more fields
    
    return user;
}
```

### After (Using Nihil)

```java
public User updateUser(User user, UserDTO userDTO) {
    return Nihil.create().update(user, userDTO);
}
```

## Advanced Usage

### Custom Property Handlers

Custom property handlers allow for complex transformations between source and target:

```java
import me.adversing.nihil.intf.IPropertyHandler;
import me.adversing.nihil.annotation.Dependency;
import me.adversing.nihil.annotation.UpdateProperty;

public class UserDTO {
    private String name;
    private String email;
    
    @UpdateProperty(targetProperty = "department", handler = DepartmentIdHandler.class)
    private Long departmentId;
    
    // Getters and setters
}

public class DepartmentIdHandler implements IPropertyHandler<Long> {
    @Dependency
    private DepartmentRepository repository;
    
    @Override
    public Object process(Long departmentId) {
        return repository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }
}
```

### Annotations

Use annotations to configure property updates at the field level:

```java
import me.adversing.nihil.annotation.UpdateProperty;

public class UserDTO {
    @UpdateProperty(targetProperty = "emailAddress")
    private String email;
    
    @UpdateProperty(includeNull = true) // Will update even if null
    private String phoneNumber;
    
    // Other fields
}
```

## Configuration Options

### Access Strategies

- `AUTO` - Try setter methods first, then fall back to direct field access
- `METHOD` - Use only setter methods (typical JavaBean pattern)
- `FIELD` - Use only direct field access (works with Lombok, immutable objects)

### Other Options

- `deepCopy` - Whether to perform deep copies for nested objects
- `ignoreNull` - Whether to skip null values (default: true)
- `includeTransient` - Whether to include transient fields
- `ignoredProperties` - Set of property names to always skip

## Extension Points

### Custom Nihil Implementations

Implement your own `INihilProvider` to extend functionality:

```java
import me.adversing.nihil.Nihil;
import me.adversing.nihil.config.NihilConfig;
import me.adversing.nihil.intf.INihilProvider;

public class MyCustomNihilProvider implements INihilProvider {
    @Override
    public Nihil create(NihilConfig config) {
        return new MyCustomNihil(config);
    }
}
```

Register your provider using Java's ServiceLoader:

```
META-INF/services/me.adversing.nihil.intf.INihilProvider
```

## Requirements

- Java 23 or higher

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
