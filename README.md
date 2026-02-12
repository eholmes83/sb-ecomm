# Spring Boot E-Commerce Application

A full-stack e-commerce application built with Spring Boot, designed to provide a robust foundation for building online shopping platforms.

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Architecture Overview](#-architecture-overview)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Building the Application](#building-the-application)
- [Running the Application](#running-the-application)
- [API Endpoints](#-api-endpoints)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

**sb-ecomm** is a Spring Boot-based e-commerce application designed to demonstrate modern web application development practices. This project serves as a solid starting point for building feature-rich online shopping platforms with capabilities for product management, user authentication, shopping cart functionality, and order processing.

### 📚 About This Project

This is a **living document** that evolves as I progress through a comprehensive Udemy course on AWS and full-stack development. I'm working through this course to expand my skillset, maintain my technical skills, and continue learning while actively seeking new opportunities after a recent layoff. This project will grow and improve as new features and concepts from the course are implemented.

### 🔄 Recent Changes

**Latest Updates (February 11, 2026 - Evening):**
- 🔧 **Enhanced Category Management with Full CRUD**
  - Added **PUT endpoint** (`/api/v1/public/categories/{categoryId}`) for updating categories
  - Implemented **ResponseEntity** for proper HTTP status code handling (200 OK, 201 CREATED, 404 NOT FOUND)
  - Enhanced error handling with **ResponseStatusException** for not found scenarios
  - Built **in-memory storage** using ArrayList with auto-incrementing ID generation
  - Changed controller method return types from String to ResponseEntity for better REST practices
  - Service layer now includes full exception handling and validation logic
  - All 4 CRUD operations now fully functional: CREATE, READ, UPDATE, DELETE

**Earlier Today (February 11, 2026 - Morning):**
- ✨ **Adopted Vertical Slice Architecture** for the entire project
  - Features are now organized as self-contained slices rather than horizontal layers
  - Each feature (Category, Product, Order, etc.) will contain all its layers: controller, service, model, DTO, repository, validator, mapper, exception, and config
  - This architecture promotes feature isolation, improved scalability, and easier maintenance
  - See [Architecture Overview](#-architecture-overview) section below for detailed information

**Previous Updates (February 2026):**
- Restructured package hierarchy from `com.echapps.sbecomm` to `com.echapps.ecom.project`
- Implemented **Category Management** module with full REST API
  - Created `Category` model class with ID and name properties
  - Developed `CategoryController` with REST endpoints for CRUD operations
  - Built `CategoryService` interface and `CategoryServiceImpl` implementation
  - API endpoints: GET, POST, and DELETE for categories
  - Implemented role-based access control (public and admin endpoints)

### Key Features

**✅ Implemented:**
- 🏷️ **Category Management** - Complete CRUD operations with REST API endpoints
  - ✅ CREATE - Add new categories with auto-generated IDs
  - ✅ READ - Retrieve all categories or specific categories
  - ✅ UPDATE - Modify existing category information
  - ✅ DELETE - Remove categories with proper error handling
  - ✅ In-memory storage with ArrayList
  - ✅ HTTP status code management (200, 201, 404)
  - ✅ Exception handling with meaningful error messages

**🚧 In Development:**
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
│   │   │           └── ecom/
│   │   │               └── project/
│   │   │                   ├── SbEcommApplication.java              # Main application entry point
│   │   │                   ├── controller/
│   │   │                   │   └── CategoryController.java          # REST endpoints for categories
│   │   │                   ├── model/
│   │   │                   │   └── Category.java                    # Category entity
│   │   │                   └── service/
│   │   │                       ├── CategoryService.java             # Service interface
│   │   │                       └── CategoryServiceImpl.java          # Service implementation
│   │   └── resources/
│   │       ├── application.properties                               # Application configuration
│   │       ├── static/                                              # Static resources (CSS, JS, images)
│   │       └── templates/                                           # Server-side templates (Thymeleaf, etc.)
│   └── test/
│       └── java/
│           └── com/
│               └── echapps/
│                   └── ecom/
│                       └── project/
│                           └── SbEcommApplicationTests.java          # Test cases
├── pom.xml                                                          # Maven configuration
├── mvnw                                                             # Maven Wrapper (Unix)
├── mvnw.cmd                                                         # Maven Wrapper (Windows)
└── README.md                                                        # This file
```

### 🏛️ Architecture Overview

The application follows a **Vertical Slice Architecture** pattern, organizing code by feature/domain boundaries rather than technical layers. Each feature slice contains all layers needed to implement that feature independently.

> **Note**: The project has been refactored from a traditional horizontal layered architecture to vertical slice architecture to better support scalability, maintainability, and team collaboration. As new features are added, they will follow the same vertical slice pattern established in the Category feature slice.

#### Package Organization - Vertical Slice Structure

```
com.echapps.ecom.project/
├── category/                     # Category Management Feature Slice
│   ├── controller/               # ✅ REST endpoints (HTTP layer)
│   │   └── CategoryController.java
│   ├── service/                  # ✅ Business logic layer
│   │   ├── CategoryService.java
│   │   └── CategoryServiceImpl.java (in-memory ArrayList storage)
│   ├── model/                    # ✅ Domain entities
│   │   └── Category.java
│   ├── dto/                      # 🚧 Data transfer objects (planned)
│   │   ├── CategoryResponse.java
│   │   └── CreateCategoryRequest.java
│   ├── repository/               # 🚧 Data access layer (planned - JPA/Database)
│   │   └── CategoryRepository.java
│   ├── exception/                # 🚧 Feature-specific exceptions (planned)
│   │   └── CategoryNotFoundException.java
│   ├── validator/                # 🚧 Input validation (planned)
│   │   └── CategoryValidator.java
│   ├── mapper/                   # 🚧 DTO/Entity mappers (planned)
│   │   └── CategoryMapper.java
│   └── config/                   # 🚧 Feature configuration (planned)
│       └── CategoryConfiguration.java
├── product/                      # Product Management Feature Slice (future)
├── order/                        # Order Management Feature Slice (future)
└── user/                         # User Management Feature Slice (future)

shared/                           # Shared/Cross-cutting concerns (planned)
├── exception/                    # Global exception handling
├── config/                       # Application-wide configuration
├── util/                         # Cross-cutting utilities
└── constants/                    # Global constants
```

> **Current Implementation Status:**  
> ✅ = Implemented | 🚧 = Planned/In Development  
> 
> The Category slice currently uses **in-memory ArrayList storage** with auto-incrementing IDs. Future iterations will add JPA/database persistence, DTOs for better API contracts, custom exceptions, validators, and mappers to complete the vertical slice architecture.

#### Benefits of Vertical Slice Architecture

✅ **Feature Isolation** - Each feature is self-contained and independently deployable  
✅ **Reduced Coupling** - Features don't depend on shared horizontal layers  
✅ **Scalability** - Easy to add new features without modifying existing code  
✅ **Testability** - Each slice can be tested in isolation  
✅ **Maintainability** - All code for a feature is in one location  
✅ **Team Collaboration** - Teams can work on different features in parallel  
✅ **Domain-Driven Design** - Naturally aligns with business domains

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

## 📡 API Endpoints

### Category Management

**Get All Categories**
```
GET /api/v1/public/categories
```
Returns a list of all product categories.

**Response:** `200 OK`
```json
[
  {
    "categoryId": 1,
    "categoryName": "Electronics"
  },
  {
    "categoryId": 2,
    "categoryName": "Clothing"
  }
]
```

**Create Category**
```
POST /api/v1/public/categories
Content-Type: application/json

{
  "categoryName": "Electronics"
}
```
Creates a new product category. Category ID is auto-generated.

**Response:** `201 CREATED`
```
Category created successfully
```

**Update Category**
```
PUT /api/v1/public/categories/{categoryId}
Content-Type: application/json

{
  "categoryName": "Updated Electronics"
}
```
Updates an existing category by ID.

**Response:** `200 OK`
```
Category with id: 1 updated successfully
```

**Response (Not Found):** `404 NOT FOUND`
```
Category not found
```

**Delete Category**
```
DELETE /api/v1/admin/categories/{id}
```
Deletes a category by ID. Requires admin privileges.

**Response:** `200 OK`
```
Category with id 1 deleted successfully
```

**Response (Not Found):** `404 NOT FOUND`
```
Category not found
```

### Example Usage with cURL

```bash
# Get all categories
curl http://localhost:8080/api/v1/public/categories

# Create a category
curl -X POST http://localhost:8080/api/v1/public/categories \
  -H "Content-Type: application/json" \
  -d '{"categoryName": "Electronics"}'

# Update a category
curl -X PUT http://localhost:8080/api/v1/public/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"categoryName": "Updated Electronics"}'

# Delete a category
curl -X DELETE http://localhost:8080/api/v1/admin/categories/1
```

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
