# Spring Boot E-Commerce Application

A full-stack e-commerce application built with Spring Boot, designed to provide a robust foundation for building online shopping platforms.

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Building the Application](#building-the-application)
- [Running the Application](#running-the-application)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

**sb-ecomm** is a Spring Boot-based e-commerce application designed to demonstrate modern web application development practices. This project serves as a solid starting point for building feature-rich online shopping platforms with capabilities for product management, user authentication, shopping cart functionality, and order processing.

### 📚 About This Project

This is a **living document** that evolves as I progress through a comprehensive Udemy course on AWS and full-stack development. I'm working through this course to expand my skillset, maintain my technical skills, and continue learning while actively seeking new opportunities after a recent layoff. This project will grow and improve as new features and concepts from the course are implemented.

### Key Features (In Development)

- 🛍️ Product catalog management
- 🛒 Shopping cart functionality
- 👤 User authentication and authorization
- 📦 Order management system
- 💳 Payment processing integration
- 📊 Admin dashboard
- 🔍 Product search and filtering

## 🚀 Technology Stack

- **Java**: 21
- **Spring Boot**: 4.0.2
- **Build Tool**: Maven
- **Framework**: Spring MVC
- **Development Tools**: Spring Boot DevTools (Hot Reload)

### Key Dependencies

- `spring-boot-starter-webmvc` - Web MVC framework for building RESTful APIs
- `spring-boot-devtools` - Development tools for automatic restart and live reload
- `spring-boot-starter-webmvc-test` - Testing support for Spring MVC applications

## 📦 Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Java Development Kit (JDK) 21** or higher
  - [Download JDK](https://www.oracle.com/java/technologies/downloads/)
  - Verify installation: `java -version`

- **Maven 3.6+** (Optional - the project includes Maven Wrapper)
  - Verify installation: `mvn -version`

- **IDE** (Recommended)
  - IntelliJ IDEA
  - Eclipse
  - VS Code with Java extensions

## 🏁 Getting Started

### Clone the Repository

```bash
git clone <repository-url>
cd sb-ecomm
```

### Quick Start

The project includes Maven Wrapper, so you don't need to have Maven installed separately.

**On macOS/Linux:**
```bash
./mvnw spring-boot:run
```

**On Windows:**
```bash
mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`

## 📁 Project Structure

```
sb-ecomm/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── echapps/
│   │   │           └── sbecomm/
│   │   │               └── SbEcommApplication.java    # Main application entry point
│   │   └── resources/
│   │       ├── application.properties                  # Application configuration
│   │       ├── static/                                 # Static resources (CSS, JS, images)
│   │       └── templates/                              # Server-side templates (Thymeleaf, etc.)
│   └── test/
│       └── java/
│           └── com/
│               └── echapps/
│                   └── sbecomm/
│                       └── SbEcommApplicationTests.java # Test cases
├── pom.xml                                             # Maven configuration
├── mvnw                                                # Maven Wrapper (Unix)
├── mvnw.cmd                                            # Maven Wrapper (Windows)
└── README.md                                           # This file
```

### Package Organization

The application follows a standard Spring Boot project structure with the following recommended package organization:

```
com.echapps.sbecomm/
├── controller/      # REST controllers and web endpoints
├── service/         # Business logic layer
├── repository/      # Data access layer
├── model/           # Domain entities and DTOs
├── config/          # Configuration classes
├── exception/       # Custom exceptions and error handling
└── util/            # Utility classes and helpers
```

## 🔨 Building the Application

### Build the Project

```bash
./mvnw clean install
```

This will:
- Compile the source code
- Run unit tests
- Package the application as a JAR file in the `target/` directory

### Run Tests

```bash
./mvnw test
```

### Create Executable JAR

```bash
./mvnw clean package
```

The executable JAR will be created at `target/sb-ecomm-0.0.1-SNAPSHOT.jar`

## ▶️ Running the Application

### Option 1: Using Maven

```bash
./mvnw spring-boot:run
```

### Option 2: Using the JAR File

```bash
java -jar target/sb-ecomm-0.0.1-SNAPSHOT.jar
```

### Option 3: From IDE

Run the `SbEcommApplication.java` class directly from your IDE.

### Accessing the Application

Once started, the application will be available at:
- **Base URL**: `http://localhost:8080`
- **Health Check**: `http://localhost:8080/actuator/health` (if actuator is added)

## 💻 Development

### Development Mode

The project includes Spring Boot DevTools, which provides:
- **Automatic Restart**: Application automatically restarts when files change
- **Live Reload**: Browser automatically refreshes when resources change
- **Enhanced Development Experience**: Improved error messages and debugging

### Configuration

Edit `src/main/resources/application.properties` to configure:
- Server port
- Database connections
- Logging levels
- Custom application properties

Example configurations:

```properties
# Server Configuration
server.port=8080

# Database Configuration (example for future use)
# spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
# spring.datasource.username=root
# spring.datasource.password=password

# Logging
logging.level.com.echapps.sbecomm=DEBUG
```

### Adding Dependencies

To add new dependencies, edit the `pom.xml` file and add the dependency in the `<dependencies>` section:

```xml
<dependency>
    <groupId>group-id</groupId>
    <artifactId>artifact-id</artifactId>
</dependency>
```

Then run:
```bash
./mvnw clean install
```

## 🧪 Testing

Write unit tests in the `src/test/java` directory following the same package structure as your source code.

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report
```

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/4.0.2/reference/)
- [Spring Framework Reference](https://docs.spring.io/spring-framework/reference/)
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
- [Maven Documentation](https://maven.apache.org/guides/index.html)

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Coding Standards

- Follow Java naming conventions
- Write meaningful commit messages
- Add unit tests for new features
- Maintain code documentation
- Keep methods small and focused

## 📄 License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

## 👥 Authors

- **echapps** - Initial work

## 🐛 Issues

If you encounter any issues or have questions, please file an issue on the project's issue tracker.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check existing documentation
- Review Spring Boot guides and tutorials

---

**Happy Coding! 🚀**
