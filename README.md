# OWL ORM
<img src="owl-orm.png" alt="OWL ORM Logo" width="200">

**OWL ORM** provides a facade for working with native Java POJOs on top of an RDF4j model based on OWL ontologies. Instead of directly manipulating RDF statements, this framework enables developers to work with familiar Java objects, significantly simplifying business logic.

---
## Features
- Simplified access to RDF4j models using Java POJOs.
- Automatically generates Java classes and interfaces from OWL ontologies.
- Extensible API for custom type conversions.
- Maven plugin for seamless integration into your build process.
- Fully supports OSGi runtime environments.

---
## Components
OWL ORM is divided into modular components to support a variety of use cases:

### **1. owl-orm-api**
The core API defining the interfaces and exceptions that make up the framework.

### **2. owl-orm-engine**
The primary implementation of the API, which proxies interfaces and wraps method calls to interact with the RDF4j model.

### **3. owl-orm-generate**
The logic responsible for generating Java interfaces based on the classes and properties defined in OWL ontologies.

### **4. owl-orm-maven-plugin**
A Maven plugin to generate ORM classes from your ontology during the build process.

### **5. owl-orm-types**
Base implementations of the `TypeConverter` API, enabling seamless translation of RDF datatype property values to and from Java primitives and objects.

---
## Usage Guide

### **OWL ORM Maven Plugin**

The Maven plugin generates Java POJOs and interfaces directly from your OWL ontology.

#### Setup
Add the following to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.example.owlorm</groupId>
            <artifactId>owl-orm-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <id>generate-orm</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate-orm</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <generates>
                    <ontology>
                        <ontologyFile>${project.basedir}/path/to/ontology.ttl</ontologyFile>
                        <outputPackage>com.example.generated</outputPackage>
                    </ontology>
                </generates>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### Execution
Run the following Maven command to generate Java classes:
```bash
mvn generate-sources
```

---
### **OWL ORM Types**
This module contains the ValueConverter interface and its implementations. The ValueConverter API powers the seamless translation of data between RDF4j Value objects and native Java objects or primitives. This allows developers to work with familiar Java types while leveraging the flexibility and power of RDF.

#### ValueConverter Interface
The ValueConverter interface defines a contract for converting between RDF4j Value objects and specific Java types. This abstraction ensures extensibility and consistency in handling datatype properties across different ontologies.

```java
public interface ValueConverter<T> {

    /**
     * Convert a value to the specified type.
     *
     * @param value The {@link Value} to convert
     * @return The converted instance
     * @throws ValueConversionException If there is an issue converting the value
     */
    T convertValue(@NonNull Value value) throws ValueConversionException;

    /**
     * Convert an instance of the TYPE of object this {@link ValueConverter} works with back into a {@link Value}.
     *
     * @param type The object to convert into a {@link Value}
     * @return The {@link Value} form of the object passed in
     * @throws ValueConversionException If there is an issue performing the conversion
     */
    Value convertType(@NonNull T type) throws ValueConversionException;

    /**
     * @return The type of class this convert works with
     */
    Class<T> getType();
}
```

#### Extending the ValueConverter API
Developers can extend the ValueConverter API to define custom type converters for specific use cases. Here’s how you can add and register your own ValueConverter implementations:

##### 1. Create a Custom Converter
```java
public class OffsetDateTimeConverter implements ValueConverter<OffsetDateTime> {
    @Override
    public OffsetDateTime convertValue(@NonNull Value value) throws ValueConversionException {
        try {
            return OffsetDateTime.parse(value.stringValue());
        } catch (Exception e) {
            throw new ValueConversionException("Failed to convert Value to OffsetDateTime", e);
        }
    }

    @Override
    public Value convertType(@NonNull OffsetDateTime type) throws ValueConversionException {
        try {
            return SimpleValueFactory.getInstance().createLiteral(type.toString());
        } catch (Exception e) {
            throw new ValueConversionException("Failed to convert OffsetDateTime to Value", e);
        }
    }

    @Override
    public Class<OffsetDateTime> getType() {
        return OffsetDateTime.class;
    }
}
```

##### 2. Register Your Converter
Add your converter to the appropriate configuration or service registry within the OWL ORM framework. This ensures that your custom converter is used automatically when working with the relevant type.

*Benefits of ValueConverter*
- Simplifies data handling by abstracting RDF4j details.
- Enables support for complex Java types through custom implementations.
- Ensures type safety and consistency in your ontology-backed applications.

---
## OSGi Support

OWL ORM is fully compatible with OSGi runtimes. It can be deployed as an OSGi bundle to enable modular development.

---
## Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/inovexcorp/owl-orm.git
   cd owl-orm
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```
   or
   ```bash
   make build
   ```

---
## Contributing

We welcome contributions to OWL ORM! To get started:

1. Fork the repository.
2. Create a new branch for your feature or fix.
3. Submit a pull request with a clear description of your changes.

### Development Guidelines
- Follow standard Java coding conventions.
- Ensure all tests pass before submitting a pull request.
- Write tests for any new features or bug fixes.

---
## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---
## TODO
- [ ] Extend the documentation for custom `TypeConverter` implementations.
- [ ] Add examples for OSGi deployment.
- [ ] Provide a detailed setup guide for the Maven plugin.
- [ ] Add more examples and tutorials.