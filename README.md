# Spring Boot E-Commerce Application

A full-stack e-commerce application built with Spring Boot, designed to provide a robust foundation for building online shopping platforms.

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Building the Application](#building-the-application)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Service Layer Architecture](#service-layer-architecture)
- [Input Validation](#input-validation)
- [Lombok Integration](#lombok-integration)
- [Development](#development)
- [Troubleshooting](#troubleshooting-common-issues)
- [Testing](#testing)
- [Additional Resources](#additional-resources)
- [Contributing](#contributing)
- [License](#license)
- [Authors](#authors)
- [Issues](#issues)
- [Support](#support)

## 🎯 Overview

**sb-ecomm** is a Spring Boot-based e-commerce application designed to demonstrate modern web application development practices. This project serves as a solid starting point for building feature-rich online shopping platforms with capabilities for product management, user authentication, shopping cart functionality, and order processing.

### 📚 About This Project

This is a **living document** that evolves as I progress through a comprehensive Udemy course on AWS and full-stack development. I'm working through this course to expand my skillset, maintain my technical skills, and continue learning while actively seeking new opportunities after a recent layoff. This project will grow and improve as new features and concepts from the course are implemented.

### 🔄 Recent Changes

**Latest Updates (March 5, 2026):**
- 🔐 **Spring Security Integration & UserDetails Implementation - Enhancement Update**
  - **UserDetails Service Implementation (`UserDetailsServiceImpl.java`)**:
    - Implements Spring Security's `UserDetailsService` interface for loading user authentication data
    - `loadUserByUsername()` method queries UserRepository to fetch User by username
    - Throws `UsernameNotFoundException` with descriptive message if user not found
    - Marked with `@Transactional` for lazy-loading relationship management
    - Enables Spring Security to retrieve user credentials for authentication verification
  - **UserDetails Implementation (`UserDetailsImpl.java`)**:
    - Implements Spring Security's `UserDetails` interface for user authentication principal
    - Contains fields: `id`, `userName`, `email`, `password`, `authorities`
    - **Key Methods:**
      - `build(User user)` - Static factory method that converts Domain User entity to UserDetails
      - Extracts roles from User and converts to `SimpleGrantedAuthority` collection
      - Enables Spring Security to check user permissions and role-based access
    - Password field marked with `@JsonIgnore` to prevent serialization in responses
    - Implements all UserDetails contract methods: `getUsername()`, `getPassword()`, `getAuthorities()`, etc.
  - **User Entity Enhancement (`User.java`)**:
    - Updated relationship configurations with proper cascade and fetch strategies
    - `@ManyToMany` to Role now uses `FetchType.EAGER` for role loading during authentication
    - `@ManyToMany` to Address with proper cascade handling for address management
    - `@OneToMany` to Product with orphan removal for seller product management
    - Proper validation annotations in place for all user fields
  - **Address Model Validation Updates (`Address.java`)**:
    - Enhanced size constraints on all fields: street (min 5), city (min 2), state (min 2), country (min 2), postalCode (min 5)
    - Custom validation error messages for each field for better API feedback
    - Maintains `@ToString.Exclude` on user relationship to prevent circular references
    - Ready for user profile address management endpoints
  - **UserRepository Enhancement (`UserRepository.java`)**:
    - `findByUserName(String username)` - Returns Optional<User> for secure null handling
    - Enables authentication lookup by username during login process
    - Spring Data JPA automatically generates query from method signature
  - **Security Integration Points:**
    - ✅ UserDetailsService enables Spring Security authentication filter to load user credentials
    - ✅ UserDetailsImpl carries user authentication data through Spring Security context
    - ✅ Roles are eager-loaded for immediate access during authorization checks
    - ✅ Integration ready for authentication endpoints and role-based access control
  - **Files Created/Modified:**
    - New: `UserDetailsServiceImpl.java`, `UserDetailsImpl.java`
    - Modified: `User.java` (enhanced relationships with cascade/fetch strategies)
    - Modified: `Address.java` (enhanced validation constraints)
    - Modified: `UserRepository.java` (added findByUserName method)
  - **Benefits:**
    - ✅ Seamless integration with Spring Security framework
    - ✅ Proper role-based authorization checks enabled
    - ✅ Secure user credential management during authentication flow
    - ✅ Foundation for building login/registration endpoints
    - ✅ Improved validation feedback for address management

**Previous Updates (March 3, 2026):**
- 🔐 **User Authentication & JWT Token Security Implementation - Major Feature Release**
  - **Created comprehensive user authentication system** with JWT token-based security
  - **User Entity (`User.java`)** - Complete user model with authentication fields:
    - Fields: `userId`, `userName`, `email`, `password` with validation constraints
    - Unique constraints on `userName` and `email` to prevent duplicates
    - Validation: `@NotBlank`, `@Size`, `@Email` annotations for input validation
    - Relationships: `@ManyToMany` to `Role` (eager loading), `@OneToMany` to `Product` (for seller products), `@ManyToMany` to `Address`
    - Constructor: `User(userName, email, password)` for convenient object creation
  - **Role-Based Access Control (RBAC):**
    - `Role` entity with enum-based role names via `AppRole` enum
    - `AppRole` enum with three roles: `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`
    - Many-to-Many relationship between User and Role with eager loading for permission checks
  - **Address Management (`Address.java`)**:
    - Complete address entity with fields: `addressId`, `street`, `city`, `state`, `country`, `postalCode`
    - Input validation on all fields: `@NotBlank`, `@Size` with custom error messages
    - Many-to-Many relationship with User for multi-address support per user
    - Marked with `@ToString.Exclude` to prevent circular reference issues
  - **JWT Token Generation & Validation (`JwtUtils.java`)**:
    - `getJwtFromRequestHeader()` - Extracts JWT from Authorization header (Bearer token format)
    - `generateTokenFromUsername()` - Creates JWT with username subject, issuedAt, expiration, signed with secret key
    - `getUserNameFromJwt()` - Extracts username from JWT payload
    - `key()` - Generates signing key from secret using HMAC-SHA256
    - `validateToken()` - Validates JWT signature, expiration, format with exception handling for expired/malformed tokens
    - Configurable expiration time via `spring.app.jwtExpirationInMs` property
  - **Authentication Token Filter (`AuthTokenFilter.java`)**:
    - Spring's `OncePerRequestFilter` for JWT validation on every request
    - `doFilterInternal()` - Intercepts requests, validates JWT, sets SecurityContext
    - Extracts username from token, loads UserDetails from database
    - Creates `UsernamePasswordAuthenticationToken` with user authorities
    - Debug logging at each step for troubleshooting authentication flow
    - Gracefully handles invalid tokens without blocking request chain
  - **JWT Exception Handler (`AuthEntryPointJwt.java`)**:
    - Implements `AuthenticationEntryPoint` for handling authentication errors
    - Returns standardized JSON error response with: status, error, message, path
    - Sets HTTP status to 401 UNAUTHORIZED for failed authentication
    - Content-Type set to APPLICATION/JSON for proper client parsing
  - **Login DTOs:**
    - `LoginRequest` - Accepts username and password for authentication
    - `LoginResponse` - Returns JWT token, username, and list of roles after successful login
  - **Configuration & Properties:**
    - `spring.app.jwtSecret` - Secret key for signing JWT tokens (configured in application.properties)
    - `spring.app.jwtExpirationInMs` - Token expiration time in milliseconds (default: 3600000 = 1 hour)
    - DEBUG logging enabled for Spring Security and authentication components in application.properties
  - **Files Created/Modified:**
    - New: `User.java`, `Role.java`, `AppRole.java`, `Address.java`
    - New: `JwtUtils.java`, `AuthTokenFilter.java`, `AuthEntryPointJwt.java`
    - New: `LoginRequest.java`, `LoginResponse.java`
    - Modified: `Product.java` (added @ManyToOne relationship to User for seller products)
    - Modified: `application.properties` (added JWT configuration)
  - **Security Features Implemented:**
    - ✅ Password-based authentication with JWT tokens
    - ✅ Role-based authorization (ROLE_USER, ROLE_SELLER, ROLE_ADMIN)
    - ✅ Bearer token authentication on protected endpoints
    - ✅ Token expiration after 1 hour for security
    - ✅ Address management for user delivery/billing addresses
    - ✅ Comprehensive input validation on all user/address fields
    - ✅ Debug logging for authentication troubleshooting
  - **Benefits:**
    - Enterprise-grade security with industry-standard JWT tokens
    - Stateless authentication (no session storage required)
    - Role-based endpoint protection for admin/seller/user features
    - Scalable authorization system for multi-tenant scenarios
    - Flexible user profile management with addresses
    - Seller capability to manage their own products
    - Clear separation of concerns between authentication and business logic
    - Comprehensive logging for security auditing

**Previous Updates (February 25, 2026):**
- 🔧 **Infinite Recursion (StackOverflowError) Fix - Critical Bug Resolution**
  - **Issue**: Application was throwing `StackOverflowError` with message "Infinite recursion at [No location information]"
  - **Root Cause**: Bidirectional Product-Category relationship caused Jackson to infinitely serialize: Product → Category → Products → Category → ... (infinite loop)
  - **Solutions Implemented**:
    - ✅ Added `@JsonBackReference("category-products")` to Product entity's category field - prevents serialization of category when Product is inside a Category's products list
    - ✅ Added `@JsonManagedReference("category-products")` to Category entity's products list - marks this side as the "managing" side of the relationship for serialization
    - ✅ Added `@JsonIgnore` to ProductRequest DTO's category field - provides additional protection at the DTO layer
  - **How It Works**: Jackson now knows to serialize the products list from Category, but when serializing each Product in that list, skip its category reference (breaks the circular chain)
  - **Impact**: All API endpoints that return Product or Category data now serialize correctly without StackOverflowError
  - Files affected: Product.java, Category.java, ProductRequest.java
  - Benefits: Eliminates application crashes, maintains data integrity on both sides of the relationship, follows Jackson best practices for bidirectional relationships
  - **Affected Endpoints Fixed**:
    - ✅ `GET /api/v1/public/products` - Get all products
    - ✅ `GET /api/v1/public/categories/{categoryId}/products` - Get products by category
    - ✅ `POST /api/v1/admin/categories/{categoryId}/product` - Add product to category
    - ✅ `GET /api/v1/public/categories` - Get all categories
    - ✅ `POST /api/v1/public/categories` - Create category
    - ✅ `PUT /api/v1/admin/products/{productId}` - Update product

**Previous Updates (February 24, 2026):**
- 🔄 **Product Update Endpoint Implementation**
  - Added `PUT /api/v1/admin/products/{productId}` endpoint for updating existing products
  - Implemented `updateProduct()` method in ProductService interface and ProductServiceImpl
  - **Update Logic Features:**
    - Validates product exists before update (throws ResourceNotFoundException if not found)
    - Updates all product fields: productName, description, quantity, price, discount
    - Automatically recalculates special price based on new price and discount values
    - Uses ObjectMapper for request DTO to entity conversion
    - Preserves product ID and category relationship during update
    - Does not update image field (maintains existing image)
  - **Controller Integration:**
    - Accepts ProductRequest DTO in request body
    - Returns updated ProductRequest DTO with 200 OK status
    - Path variable `productId` identifies the product to update
  - **Service Layer Implementation:**
    - Fetches existing product from database via ProductRepository
    - Maps incoming DTO to entity using ObjectMapper
    - Updates only modifiable fields (preserves image and category)
    - Calls `calculateSpecialPrice()` helper method for price computation
    - Persists updated product back to database
  - Files affected: ProductController.java, ProductService.java, ProductServiceImpl.java
  - Benefits: Complete CRUD functionality for Product feature, full update capability matching Category feature pattern, maintains data integrity through validation

**Previous Updates (February 23, 2026):**
- 🏷️ **Product Management Feature - Complete Implementation**
  - Created `Product` entity with full JPA integration and relationships to Category via `@ManyToOne`
  - Product fields: `productId`, `productName`, `image`, `description`, `quantity`, `price`, `discount`, `specialPrice`
  - Created `ProductRequest` DTO for incoming POST requests with all product fields
  - Created `ProductResponse` DTO for outgoing responses containing a list of products
  - Implemented `ProductService` interface with methods for complete product operations
  - **Product Service Layer (ProductServiceImpl)** with advanced features:
    - `addProduct()` - Creates product with automatic special price calculation (price - discount %)
    - `getAllProducts()` - Retrieves all products with DTO conversion
    - `searchByCategory()` - Find products by category with automatic price sorting (ascending)
    - `searchProductsByKeyword()` - Full-text search on product names (case-insensitive)
    - `deleteProduct()` - Removes product from database
  - Implemented `ProductRepository` extending JpaRepository with custom query methods:
    - `findByCategoryOrderByPriceAsc()` - Custom JPQL for category filtering with price sorting
    - `findByProductNameLikeIgnoreCase()` - Wildcard search for keyword matching
  - Created `ProductController` with 5 REST endpoints:
    - `POST /api/v1/admin/categories/{categoryId}/product` - Add new product (201 CREATED)
    - `GET /api/v1/public/products` - Get all products (200 OK)
    - `GET /api/v1/public/categories/{categoryId}/products` - Get products by category (200 OK)
    - `GET /api/v1/public/products/keyword/{keyword}` - Search by keyword (302 FOUND)
    - `DELETE /api/v1/admin/products/{productId}` - Delete product (200 OK)
  - **ObjectMapper Integration** - Uses Jackson for automatic DTO/Entity conversions
  - **Error Handling** - ResourceNotFoundException for missing categories or products
  - Files affected: Product.java, ProductRequest.java, ProductResponse.java, ProductService.java, ProductServiceImpl.java, ProductController.java, ProductRepository.java
  - Benefits: Complete vertical slice architecture for Product feature, mirroring Category feature patterns, advanced search capabilities with JPQL queries

- ✅ **Enhanced Category Validation with Size Constraint**
  - Added `@Size` validation annotation to Category.categoryName field
  - Constraint: Minimum 3 characters with message "Category name must be at least 3 characters"
  - Strengthens data quality and prevents creation of too-short category names
  - Works in conjunction with existing `@NotBlank` validation
  - Files affected: Category.java
  - Benefits: More robust input validation, better data consistency

**Latest Updates (February 17, 2026):**
- 📄 **Standardized API Response Format with APIResponse DTO**
  - Created `APIResponse` DTO (`message: String`, `isSuccess: boolean`) for standardized error/success responses
  - Updated `GlobalExceptionHandler` to use `APIResponse` for all exception handlers
  - `ResourceNotFoundException` now returns `APIResponse` with 404 status and `isSuccess=false`
  - `APIException` now returns `APIResponse` with 400 status and `isSuccess=false`
  - Provides consistent API response structure across all error scenarios
  - Benefits: Clients can reliably parse error responses with consistent field structure

- 📊 **Pagination & Sorting Implementation**
  - Added pagination support to `getAllCategories()` endpoint with query parameters: `pageNumber`, `pageSize`, `sortBy`, `sortOrder`
  - Implemented Spring Data JPA `Pageable` and `PageRequest` for database-level pagination
  - Updated `CategoryResponse` DTO with pagination metadata: `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `lastPage`
  - Service layer now uses `Sort` and `Sort.Direction` to handle dynamic sorting by field and order
  - Benefits: Scalable API that can handle large datasets without loading everything into memory

- ⚙️ **Application Configuration Constants**
  - Created `AppConstants` class with default values for pagination:
    - `DEFAULT_PAGE_NUMBER = "0"`
    - `DEFAULT_PAGE_SIZE = "50"`
    - `DEFAULT_SORT_BY = "categoryId"`
    - `DEFAULT_SORT_DIRECTION = "asc"`
  - Controller uses these constants as `@RequestParam` default values
  - Centralized configuration makes it easy to adjust API defaults in one place
  - Benefits: Maintainable, consistent defaults across the application

- 🔍 **Enhanced CategoryRepository with Custom Query Method**
  - Added `findByCategoryName(String categoryName)` method for duplicate category checking
  - Used in `createCategory()` to prevent duplicate category names
  - Spring Data JPA automatically generates SQL query from method signature
  - Benefits: Data integrity and prevention of duplicate entries

- 🔄 **ObjectMapper Integration**
  - Integrated Jackson `ObjectMapper` for automatic DTO-to-Entity and Entity-to-DTO conversion
  - Eliminates manual mapping in service layer
  - `createCategory()`, `deleteCategory()`, and `updateCategory()` now use mapper for conversions
  - Used in pagination to convert `List<Category>` to `List<CategoryRequest>`
  - Benefits: Reduced boilerplate code, consistent mapping logic

- 📝 **Postman Collection Updated with Pagination & Sorting**
  - Updated Postman collection to include pagination and sorting example requests
  - Collection now demonstrates: `GET /api/v1/public/categories?pageNumber=0&pageSize=10&sortBy=categoryId&sortOrder=desc`
  - Test data added to H2 database for pagination testing

**Previous Updates (February 16, 2026):**
- 🔄 **DTO Pattern Implementation & Controller/Service Refactoring**
  - Implemented **Data Transfer Objects (DTOs)** for clean separation of API contracts from domain models
  - Created `CategoryRequest` DTO for incoming POST/PUT requests with `categoryId` and `categoryName` fields
  - Created `CategoryResponse` DTO for outgoing GET responses, containing a list of `CategoryRequest` objects
  - **Refactored CategoryController** to accept and return DTOs instead of domain models
    - All endpoints now use `CategoryRequest` and `CategoryResponse` for data serialization
    - Cleaner API contracts that don't expose internal domain model structure
  - **Refactored CategoryService interface** to work with DTOs:
    - `getAllCategories()` returns `CategoryResponse` containing list of categories
    - `createCategory(CategoryRequest)` accepts DTO and returns `CategoryRequest`
    - `updateCategory(CategoryRequest, Long)` accepts DTO and returns `CategoryRequest`
    - `deleteCategory(Long)` returns deleted `CategoryRequest`
  - **Applied Lombok to DTOs** with `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` annotations
  - **Enhanced GlobalExceptionHandler** to properly handle validation errors on DTOs
    - `MethodArgumentNotValidException` handler provides detailed field-level error messages
  - Benefits: Loose coupling between API and domain models, easier API versioning, better security (prevents over-exposure of data)

**Previous Updates (February 15, 2026):**
- ✅ **Input Validation Implementation**
  - Added **Jakarta Bean Validation** to Category model with `@NotBlank` annotation
  - Category name field now validates that input is not empty or whitespace
  - Added **Spring Boot Starter Validation** dependency to pom.xml
  - Controller uses `@Valid` annotation to trigger validation on incoming requests
  - Validation errors automatically return 400 BAD REQUEST with error details
  - Custom validation message: "Category name is required"
  - Enhanced data integrity and API robustness through declarative validation

- 🔧 **Lombok Integration**
  - Added **Lombok** dependency (version 1.18.42) to reduce boilerplate code
  - Category model now uses `@Data`, `@NoArgsConstructor`, and `@AllArgsConstructor` annotations
  - Automatic generation of getters, setters, toString, equals, and hashCode methods
  - Cleaner, more maintainable code with less manual method writing
  - Improved developer productivity with compile-time code generation

**Previous Updates (February 12, 2026 - Night):**
- 🔄 **Service Layer Refactoring - Return Type Optimization**
  - Refactored **CategoryService interface** methods to return `void` instead of return values
  - Updated **CategoryServiceImpl** to throw exceptions instead of returning status messages
  - Modified **CategoryController** to handle void returns and manage responses independently
  - Cleaner separation of concerns: Service handles data operations, Controller handles HTTP responses
  - Exception handling consolidated in `ResponseStatusException` for all error scenarios
  - More RESTful design: Service layer focuses on business logic, not HTTP details
  - Improved testability: Service tests don't need to assert on response messages

**Earlier Today (February 12, 2026):**
- 🗄️ **Database Integration & JPA Implementation** 
  - Added **CategoryRepository** interface extending `JpaRepository<Category, Long>` for database operations
  - Converted **Category model** to JPA Entity with proper annotations:
    - `@Entity(name = "categories")` - Maps class to database table
    - `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)` - Auto-incrementing primary key
  - Refactored **CategoryServiceImpl** to use repository instead of in-memory ArrayList
  - All CRUD operations now use JPA for database persistence
  - Added **H2 Database** as in-memory database for testing/development
  - Added **Spring Data JPA** dependency for ORM support
  - Configured **application.properties** with H2 connection and JPA settings:
    - H2 console enabled for database inspection (`http://localhost:8080/h2-console`)
    - SQL logging enabled for debugging
  - Maintained all existing REST endpoints - no API changes, internal implementation refactored
  - Service layer maintains exception handling with `ResponseStatusException` for 404 scenarios

**Previous Updates (February 11, 2026 - Evening):**
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
- 👤 **User Authentication & JWT Security** - Enterprise-grade token-based authentication
  - ✅ User entity with complete JPA mapping and relationships (roles, addresses, seller products)
  - ✅ Role-Based Access Control (RBAC) with three roles: ROLE_USER, ROLE_SELLER, ROLE_ADMIN
  - ✅ Spring Security integration with UserDetailsService for credential loading
  - ✅ UserDetails implementation (UserDetailsImpl) for authentication principal
  - ✅ JWT token generation and validation with configurable expiration (1 hour default)
  - ✅ AuthTokenFilter for JWT validation on every request
  - ✅ Bearer token authentication in request headers
  - ✅ AuthEntryPointJwt for standardized JSON error responses (401 UNAUTHORIZED)
  - ✅ Comprehensive input validation on user credentials (username, email, password)
  - ✅ Unique constraints on username and email to prevent duplicates
  - ✅ Debug logging for authentication and authorization troubleshooting
  - ✅ Password-based authentication with JWT token generation (ready for login endpoints)
  - 🚧 User registration endpoint (planned)
  - 🚧 User login endpoint (planned)
  - 🚧 User profile management endpoints (planned)

- 📍 **Address Management** - Complete user address support
  - ✅ Multiple addresses per user via Many-to-Many relationship
  - ✅ Address fields: street, city, state, country, postal code
  - ✅ Input validation on all address fields with minimum length constraints
  - ✅ Custom error messages for validation failures

- 🏷️ **Category Management** - Complete CRUD operations with database persistence and clean architecture
  - ✅ CREATE - Add new categories (auto-generated IDs via database)
  - ✅ READ - Retrieve all categories with pagination and sorting support
  - ✅ UPDATE - Modify existing category information
  - ✅ DELETE - Remove categories with proper error handling
  - ✅ **Database Persistence** - H2 in-memory database with JPA/Hibernate ORM
  - ✅ **Input Validation** - Jakarta Bean Validation with `@NotBlank` and `@Size` constraints on category name
  - ✅ **Lombok Integration** - Reduced boilerplate with auto-generated getters, setters, and constructors
  - ✅ **Separation of Concerns** - Service layer focuses on business logic, Controller handles HTTP responses
  - ✅ **Clean Service Methods** - Service methods return DTOs and throw exceptions for errors
  - ✅ HTTP status code management (200, 201, 400, 404)
  - ✅ Exception handling with meaningful error messages
  - ✅ REST API endpoints with proper response entities
  - ✅ **RESTful Design** - Clean separation between data operations and HTTP protocol concerns
  - ✅ **Data Transfer Objects (DTOs)** - `CategoryRequest` and `CategoryResponse` for API contracts
  - ✅ **Pagination & Sorting** - Query parameters for `pageNumber`, `pageSize`, `sortBy`, `sortOrder`
  - ✅ **Standardized API Responses** - `APIResponse` DTO for consistent error response format
  - ✅ **ObjectMapper Integration** - Automatic DTO-to-Entity and Entity-to-DTO conversions
  - ✅ **Application Constants** - Centralized default values via `AppConstants` class
  - ✅ **Custom Repository Methods** - `findByCategoryName()` for duplicate prevention
  - ✅ **Duplicate Category Prevention** - Prevents creation of categories with existing names

- 🛍️ **Product Management** - Complete CRUD operations with advanced search functionality
  - ✅ CREATE - Add new products to categories with automatic special price calculation
  - ✅ READ - Retrieve all products or filter by category
  - ✅ UPDATE - Modify existing product details with automatic special price recalculation
  - ✅ SEARCH - Full-text keyword search on product names (case-insensitive)
  - ✅ DELETE - Remove products with proper error handling
  - ✅ **Database Persistence** - JPA integration with automatic relationships to categories
  - ✅ **Input Validation** - Category relationship validation (prevents orphaned products)
  - ✅ **Lombok Integration** - Clean Product entity with auto-generated methods
  - ✅ **DTO Pattern** - ProductRequest and ProductResponse for API contracts
  - ✅ **Advanced Queries** - Custom repository methods for category filtering and keyword search
  - ✅ **Price Calculation** - Automatic special price computation (price - discount %)
  - ✅ **Sorting** - Products automatically sorted by price when filtered by category
  - ✅ **ObjectMapper Integration** - Seamless entity-to-DTO conversions
  - ✅ **Error Handling** - Proper exceptions for missing categories or products
  - ✅ REST endpoints for product operations: GET, POST, PUT, DELETE, SEARCH

**🚧 In Development:**
- 👤 User Registration & Login Endpoints (authentication flow)
- 👥 User Profile Management (view/update user information)
- 📍 Address Management Endpoints (add/update/delete user addresses)
- 🛒 Shopping cart functionality
- 📦 Order management system
- 💳 Payment processing integration
- 📊 Admin dashboard
- ⭐ Product ratings and reviews
- 🔍 Advanced search and filtering
- 📧 Email notifications

## 🚀 Technology Stack

- **Java**: 21
- **Spring Boot**: 4.0.2
- **Build Tool**: Maven
- **Framework**: Spring MVC
- **Security**: Spring Security with JWT Token Authentication
- **ORM**: Spring Data JPA / Hibernate
- **Database**: H2 (In-Memory for Development)
- **JSON Processing**: Jackson (Serialization/Deserialization)
- **Validation**: Jakarta Bean Validation (Spring Boot Starter Validation)
- **Development Tools**: 
  - Spring Boot DevTools (Hot Reload)
  - Lombok (Code Generation)

### Key Dependencies

- `spring-boot-starter-webmvc` - Web MVC framework for building RESTful APIs
- `spring-boot-starter-data-jpa` - Spring Data JPA for database persistence
- `spring-boot-starter-validation` - Jakarta Bean Validation for input validation
- `spring-boot-starter-security` - Spring Security framework for authentication and authorization
- `jjwt` - JSON Web Token (JWT) library for token generation and validation
- `h2` - In-memory relational database for development/testing
- `spring-boot-h2console` - H2 database browser console
- `spring-boot-devtools` - Development tools for automatic restart and live reload
- `lombok` (v1.18.42) - Annotation-based code generation (getters, setters, constructors, etc.)
- `spring-boot-starter-webmvc-test` - Testing support for Spring MVC applications
- `jackson-databind` - JSON serialization/deserialization with advanced features (@JsonBackReference, @JsonManagedReference, @JsonIgnore)

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
│   │   └── CategoryController.java (uses @Valid for validation)
│   ├── service/                  # ✅ Business logic layer
│   │   ├── CategoryService.java
│   │   └── CategoryServiceImpl.java (uses JPA repository)
│   ├── repository/               # ✅ Data access layer (JPA/Database)
│   │   └── CategoryRepository.java (extends JpaRepository)
│   ├── model/                    # ✅ Domain entities (JPA Entity with validation)
│   │   └── Category.java (uses Lombok & Jakarta Bean Validation)
│   ├── dto/                      # ✅ Data transfer objects
│   │   ├── request/
│   │   │   └── CategoryRequest.java
│   │   └── response/
│   │       ├── CategoryResponse.java
│   │       └── APIResponse.java (standardized error responses)
│   ├── exception/                # 🚧 Feature-specific exceptions (planned)
│   ├── validator/                # 🚧 Custom validation logic (planned)
│   ├── mapper/                   # 🚧 DTO/Entity mappers (planned)
│   └── config/                   # 🚧 Feature configuration (planned)
│
├── product/                      # Product Management Feature Slice
│   ├── controller/               # ✅ REST endpoints (HTTP layer)
│   │   └── ProductController.java (handles all product requests)
│   ├── service/                  # ✅ Business logic layer
│   │   ├── ProductService.java
│   │   └── ProductServiceImpl.java (advanced search & pricing logic)
│   ├── repository/               # ✅ Data access layer (JPA/Database)
│   │   └── ProductRepository.java (custom JPQL queries)
│   ├── model/                    # ✅ Domain entities (JPA Entity with relationships)
│   │   └── Product.java (uses Lombok & Category relationship)
│   ├── dto/                      # ✅ Data transfer objects
│   │   ├── request/
│   │   │   └── ProductRequest.java
│   │   └── response/
│   │       └── ProductResponse.java
│   ├── exception/                # 🚧 Feature-specific exceptions (planned)
│   ├── validator/                # 🚧 Custom validation logic (planned)
│   ├── mapper/                   # 🚧 DTO/Entity mappers (planned)
│   └── config/                   # 🚧 Feature configuration (planned)
│
├── user/                         # User Management Feature Slice
│   ├── controller/               # 🚧 REST endpoints for registration/profile (planned)
│   ├── service/                  # 🚧 Business logic layer (planned)
│   ├── repository/               # ✅ Data access layer (JPA/Database)
│   │   └── UserRepository.java (extends JpaRepository, findByUserName method)
│   ├── model/                    # ✅ Domain entities (JPA Entity with relationships)
│   │   └── User.java (with roles, addresses, seller products relationships)
│   ├── dto/                      # 🚧 Data transfer objects (planned)
│   ├── exception/                # 🚧 Feature-specific exceptions (planned)
│   ├── validator/                # 🚧 Custom validation logic (planned)
│   └── mapper/                   # 🚧 DTO/Entity mappers (planned)
│
├── security/                     # Security & Authentication Feature Slice
│   ├── jwt/                      # ✅ JWT token handling
│   │   ├── JwtUtils.java (token generation, validation, extraction)
│   │   ├── AuthTokenFilter.java (OncePerRequestFilter for JWT validation)
│   │   ├── AuthEntryPointJwt.java (authentication error handling)
│   │   ├── LoginRequest.java (DTO for login credentials)
│   │   └── LoginResponse.java (DTO for token response)
│   └── services/                 # ✅ Spring Security integration
│       ├── UserDetailsServiceImpl.java (loads user from database)
│       └── UserDetailsImpl.java (user authentication principal)
│
├── db/                           # Database Models Feature Slice
│   └── model/                    # ✅ Shared database entities
│       ├── Role.java (role entity for RBAC)
│       ├── AppRole.java (enum for role types: USER, SELLER, ADMIN)
│       └── Address.java (user address entity with validation)
│
└── shared/                       # Shared/Cross-cutting concerns
    ├── exception/                # ✅ Global exception handling
    │   ├── GlobalExceptionHandler.java
    │   ├── APIException.java
    │   └── ResourceNotFoundException.java
    ├── config/                   # ✅ Application-wide configuration
    │   └── AppConstants.java
    ├── util/                     # 🚧 Cross-cutting utilities (planned)
    └── constants/                # 🚧 Global constants (planned)
```

> **Current Implementation Status:**  
> ✅ = Implemented | 🚧 = Planned/In Development  
> 
> Both Category and Product features now have **full vertical slice implementations** with all CRUD operations, validation, DTOs, repositories, and service layers. The Product feature includes advanced search capabilities (keyword search, category filtering) and special pricing calculations. Shared exception handling is centralized in the GlobalExceptionHandler for both features.

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
Returns a list of all product categories with pagination and sorting support.

**Query Parameters:**
- `pageNumber` - Page number (default: 0)
- `pageSize` - Items per page (default: 50)
- `sortBy` - Field to sort by (default: categoryId)
- `sortOrder` - Sort direction: "asc" or "desc" (default: asc)

**Response:** `200 OK`
```json
{
  "content": [
    {
      "categoryId": 1,
      "categoryName": "Electronics"
    },
    {
      "categoryId": 2,
      "categoryName": "Clothing"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 2,
  "totalPages": 1,
  "lastPage": true
}
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

**Validation Rules:**
- `categoryName` is required (cannot be blank, null, or whitespace only)
- Minimum length: 3 characters
- Must not duplicate existing categories

**Response:** `201 CREATED`
```json
{
  "categoryId": 1,
  "categoryName": "Electronics"
}
```

**Response (Validation Error):** `400 BAD REQUEST`
```json
{
  "timestamp": "2026-02-23T16:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Category name must be at least 3 characters",
  "path": "/api/v1/public/categories"
}
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
```json
{
  "categoryId": 1,
  "categoryName": "Updated Electronics"
}
```

**Response (Not Found):** `404 NOT FOUND`
```json
{
  "message": "Category not found",
  "isSuccess": false
}
```

**Delete Category**
```
DELETE /api/v1/admin/categories/{id}
```
Deletes a category by ID. Requires admin privileges.

**Response:** `200 OK`
```json
{
  "categoryId": 1,
  "categoryName": "Electronics"
}
```

**Response (Not Found):** `404 NOT FOUND`
```json
{
  "message": "Category not found",
  "isSuccess": false
}
```

### Product Management

**Add Product to Category**
```
POST /api/v1/admin/categories/{categoryId}/product
Content-Type: application/json

{
  "productName": "iPhone 15",
  "quantity": 50,
  "price": 999.99,
  "discount": 10,
  "description": "Latest iPhone model"
}
```
Creates a new product and automatically associates it with a category. Special price is calculated automatically as: `price - (discount% of price)`.

**Response:** `201 CREATED`
```json
{
  "productId": 1,
  "productName": "iPhone 15",
  "image": "default.png",
  "quantity": 50,
  "price": 999.99,
  "discount": 10,
  "specialPrice": 899.99
}
```

**Response (Category Not Found):** `404 NOT FOUND`
```json
{
  "message": "Category not found",
  "isSuccess": false
}
```

**Get All Products**
```
GET /api/v1/public/products
```
Retrieves all products from the catalog.

**Response:** `200 OK`
```json
{
  "content": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "image": "default.png",
      "quantity": 50,
      "price": 999.99,
      "discount": 10,
      "specialPrice": 899.99
    }
  ]
}
```

**Get Products by Category**
```
GET /api/v1/public/categories/{categoryId}/products
```
Retrieves all products in a specific category, sorted by price in ascending order.

**Response:** `200 OK`
```json
{
  "content": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "image": "default.png",
      "quantity": 50,
      "price": 999.99,
      "discount": 10,
      "specialPrice": 899.99
    }
  ]
}
```

**Search Products by Keyword**
```
GET /api/v1/public/products/keyword/{keyword}
```
Performs case-insensitive search on product names using wildcard matching.

**Example:** `/api/v1/public/products/keyword/iphone`

**Response:** `302 FOUND`
```json
{
  "content": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "image": "default.png",
      "quantity": 50,
      "price": 999.99,
      "discount": 10,
      "specialPrice": 899.99
    }
  ]
}
```

**Update Product**
```
PUT /api/v1/admin/products/{productId}
Content-Type: application/json

{
  "productName": "iPhone 15 Pro",
  "description": "Latest iPhone Pro model with advanced features",
  "quantity": 75,
  "price": 1199.99,
  "discount": 15
}
```
Updates an existing product. Special price is automatically recalculated based on new price and discount values. Requires admin privileges.

**Response:** `200 OK`
```json
{
  "productId": 1,
  "productName": "iPhone 15 Pro",
  "image": "default.png",
  "description": "Latest iPhone Pro model with advanced features",
  "quantity": 75,
  "price": 1199.99,
  "discount": 15,
  "specialPrice": 1019.99
}
```

**Response (Not Found):** `404 NOT FOUND`
```json
{
  "message": "Product not found with productId: 1",
  "isSuccess": false
}
```

**Delete Product**
```
DELETE /api/v1/admin/products/{productId}
```
Deletes a product by ID. Requires admin privileges.

**Response:** `200 OK`
```json
{
  "productId": 1,
  "productName": "iPhone 15",
  "image": "default.png",
  "quantity": 50,
  "price": 999.99,
  "discount": 10,
  "specialPrice": 899.99
}
```

**Response (Not Found):** `404 NOT FOUND`
```json
{
  "message": "Product not found",
  "isSuccess": false
}
```

### Example Usage with cURL

```bash
# Get all categories with pagination
curl "http://localhost:8080/api/v1/public/categories?pageNumber=0&pageSize=10&sortBy=categoryId&sortOrder=asc"

# Create a category
curl -X POST http://localhost:8080/api/v1/public/categories \
  -H "Content-Type: application/json" \
  -d '{"categoryName": "Electronics"}'

# Add a product to a category
curl -X POST http://localhost:8080/api/v1/admin/categories/1/product \
  -H "Content-Type: application/json" \
  -d '{"productName": "iPhone 15", "quantity": 50, "price": 999.99, "discount": 10}'

# Get all products
curl http://localhost:8080/api/v1/public/products

# Get products by category (sorted by price)
curl http://localhost:8080/api/v1/public/categories/1/products

# Search products by keyword
curl http://localhost:8080/api/v1/public/products/keyword/iphone

# Update a product
curl -X PUT http://localhost:8080/api/v1/admin/products/1 \
  -H "Content-Type: application/json" \
  -d '{"productName": "iPhone 15 Pro", "description": "Latest iPhone Pro model", "quantity": 75, "price": 1199.99, "discount": 15}'

# Delete a product
curl -X DELETE http://localhost:8080/api/v1/admin/products/1

# Update a category
curl -X PUT http://localhost:8080/api/v1/public/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"categoryName": "Updated Electronics"}'

# Delete a category
curl -X DELETE http://localhost:8080/api/v1/admin/categories/1
```

## 🏗️ Service Layer Architecture

### Design Philosophy: Separation of Concerns

The Category service layer demonstrates clean architecture principles with clear separation between business logic and HTTP protocol concerns.

**Service Layer Responsibilities (CategoryService):**
- Pure business logic - no awareness of HTTP
- Data persistence operations via JpaRepository
- Exception throwing for error scenarios (not returning status strings)
- Methods return `void` or domain objects
- Single Responsibility: Focus on WHAT to do, not HOW to communicate

**Controller Responsibilities (CategoryController):**
- HTTP request/response handling
- Converting exceptions to appropriate HTTP status codes
- Managing ResponseEntity with status codes
- Single Responsibility: Focus on HTTP protocol details

### Method Signatures

**Service Layer (Business Logic)**
```java
public interface CategoryService {
    List<Category> getAllCategories();
    void createCategory(Category category);
    void deleteCategory(Long id);  // Throws ResponseStatusException if not found
    void updateCategory(Category category, Long categoryId);  // Throws ResponseStatusException if not found
}
```

**Controller Layer (HTTP Response Handling)**
```java
@PostMapping("/public/categories")
public ResponseEntity<String> createCategory(@RequestBody Category category) {
    categoryService.createCategory(category);
    return new ResponseEntity<>("Category created successfully", HttpStatus.CREATED);
}

@DeleteMapping("/admin/categories/{id}")
public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
    try {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>("Category with id: " + id + " deleted successfully", HttpStatus.OK);
    } catch (ResponseStatusException e) {
        return new ResponseEntity<>(e.getReason(), e.getStatusCode());
    }
}
```

### Benefits of This Approach

✅ **Testability** - Service can be tested without mocking HttpStatus or ResponseEntity  
✅ **Reusability** - Service layer can be used by controllers, scheduled tasks, or other clients  
✅ **Clarity** - Clear what layer does what: service = logic, controller = HTTP  
✅ **Exception Handling** - Exceptions bubble up naturally, caught at appropriate level  
✅ **Spring Integration** - `ResponseStatusException` is Spring's standard for HTTP errors  
✅ **Clean Code** - Service methods don't return status messages, controller does

## 🛡️ Input Validation

### Jakarta Bean Validation

The application uses **Jakarta Bean Validation** (formerly JSR-303/JSR-380) for declarative input validation. This provides a standardized way to validate data before it reaches the business logic layer.

### Category Model Validation

The `Category` entity uses validation annotations to ensure data integrity:

```java
@Entity(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank(message = "Category name is required")
    private String categoryName;
}
```

**Validation Constraints:**
- `@NotBlank` - Ensures the category name is not null, empty, or whitespace
- Custom error message: "Category name is required"

### Controller Validation Trigger

The controller uses `@Valid` annotation to trigger validation on incoming requests:

```java
@PostMapping("/public/categories")
public ResponseEntity<String> createCategory(@Valid @RequestBody Category category) {
    categoryService.createCategory(category);
    return new ResponseEntity<>("Category created successfully", HttpStatus.CREATED);
}
```

When validation fails:
- Spring automatically returns **400 BAD REQUEST**
- Response includes validation error details
- Request never reaches the service layer

### Benefits of Bean Validation

✅ **Declarative** - Validation rules defined directly on model fields  
✅ **Reusable** - Same validation applies everywhere the entity is used  
✅ **Standard** - Based on Jakarta EE specification  
✅ **Automatic** - Spring Boot auto-configures validation support  
✅ **Clean** - No manual validation code in controllers or services  
✅ **Consistent** - Uniform error response format across the API

## 🔧 Lombok Integration

### Code Generation with Annotations

The project uses **Lombok** to reduce boilerplate code through compile-time code generation. Lombok provides annotations that automatically generate commonly used methods.

### Category Model with Lombok

```java
@Entity(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank(message = "Category name is required")
    private String categoryName;
}
```

**Lombok Annotations Used:**

| Annotation | Generated Code |
|------------|----------------|
| `@Data` | Getters for all fields, setters for all non-final fields, `toString()`, `equals()`, `hashCode()` |
| `@NoArgsConstructor` | Default constructor with no parameters |
| `@AllArgsConstructor` | Constructor with parameters for all fields |

### Before vs. After Lombok

**Without Lombok (Manual):**
```java
public class Category {
    private Long categoryId;
    private String categoryName;
    
    // Default constructor
    public Category() {}
    
    // All-args constructor
    public Category(Long categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
    
    // Getters
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    
    // Setters
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    // equals, hashCode, toString methods...
    // (30+ more lines of boilerplate)
}
```

**With Lombok (3 annotations):**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Long categoryId;
    private String categoryName;
}
```

### Benefits of Lombok

✅ **Less Boilerplate** - Reduces code size by 70-80%  
✅ **Maintainability** - Adding/removing fields doesn't require updating methods  
✅ **Readability** - Focus on business logic, not getters/setters  
✅ **Consistency** - Generated methods follow best practices  
✅ **Productivity** - Faster development with less typing  
✅ **IDE Support** - Works with IntelliJ IDEA, Eclipse, VS Code

### IDE Setup for Lombok

**IntelliJ IDEA:**
1. Install Lombok plugin (usually pre-installed)
2. Enable annotation processing: Settings → Build → Compiler → Annotation Processors → Enable
3. IntelliJ will recognize generated methods for autocomplete

**Eclipse:**
1. Run `java -jar lombok.jar` to install Lombok into Eclipse
2. Restart Eclipse

**VS Code:**
1. Install "Lombok Annotations Support" extension
2. Configure Java language server to enable annotation processing

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

**Current Database Configuration:**

```properties
# Application name
spring.application.name=sb-ecomm

# H2 Database Configuration
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb

# JPA/Hibernate Configuration
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
# Uncomment to auto-update schema: spring.jpa.hibernate.ddl-auto=update
```

### Database Access

**H2 Database Console:**
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa` (default)
- **Password**: (leave empty)

Use the H2 console to:
- View database tables and data
- Execute SQL queries
- Inspect category data persisted in the database

### Customizing Configuration

You can modify other settings as needed:

```properties
# Server Configuration
server.port=8080

# Logging
logging.level.com.echapps.ecom.project=DEBUG
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

## 🔍 Troubleshooting Common Issues

### Infinite Recursion (StackOverflowError)

**Problem:** When calling API endpoints that return Product or Category data, the application crashes with:
```
Exception in thread "http-nio-8080-exec-1" java.lang.StackOverflowError
	at [No location information]
```

**Root Cause:** 
The application has a bidirectional relationship between Product and Category entities. When Jackson (Spring's JSON serializer) tries to convert entities to JSON, it encounters a circular reference:
- Product has a reference to Category
- Category has a list of Products
- Each Product in the list references Category again → infinite loop

**Solution (Already Implemented):**
The application uses Jackson's `@JsonManagedReference` and `@JsonBackReference` annotations to control serialization:

1. **Category.java** - Marks products list as the "managed" side:
   ```java
   @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
   @JsonManagedReference("category-products")
   private List<Product> products;
   ```

2. **Product.java** - Marks category as the "back reference" (not serialized when coming from Category):
   ```java
   @ManyToOne
   @JoinColumn(name = "category_id")
   @JsonBackReference("category-products")
   private Category category;
   ```

**Verification:**
Test these endpoints to confirm the fix works:
```bash
# Should return products without circular references
curl http://localhost:8080/api/v1/public/products

# Should return categories with product lists
curl http://localhost:8080/api/v1/public/categories
```

**If Issue Persists:**
1. Verify both `@JsonManagedReference` and `@JsonBackReference` use the same reference name: `"category-products"`
2. Rebuild and restart the application: `./mvnw clean install && ./mvnw spring-boot:run`
3. Check that Product.java has `@JsonBackReference` and Category.java has `@JsonManagedReference`
4. Clear the H2 database and restart fresh

**Alternative Solutions (If Annotations Don't Work):**

1. **Use `@JsonIdentityInfo`** - Serializes objects by ID instead of duplicating:
   ```java
   @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "productId")
   public class Product { ... }
   ```

2. **Use DTOs** - Create separate DTOs that don't have circular references:
   ```java
   // ProductWithoutCategoryDTO - Product without category
   // CategoryWithProductsDTO - Category with product summaries only
   ```

3. **Use `@Transient`** - Mark the field as non-persistent:
   ```java
   @Transient
   @JsonIgnore
   private Category category;
   ```

**Prevention Going Forward:**
When creating new bidirectional relationships:
- Always add `@JsonManagedReference` to the "managing" side (the side that should be fully serialized)
- Always add `@JsonBackReference` to the "back" side (the reverse reference that should be hidden)
- Use matching reference names on both annotations
- Test serialization immediately after creating the relationship

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
