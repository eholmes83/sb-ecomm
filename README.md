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

**Latest Updates (March 18, 2026):**
- 👥 **Address Management Feature - Complete CRUD Operations**
  - Implemented comprehensive address management for logged-in users
  - Added `AddressController` with six REST endpoints under `/api/v1/addresses`:
    - `POST /api/v1/addresses` - Create a new address for current user
    - `GET /api/v1/addresses` - Get all addresses in system
    - `GET /api/v1/addresses/{addressId}` - Get specific address by ID
    - `GET /api/v1/addresses/user/addresses` - Get all addresses for current logged-in user
    - `PUT /api/v1/addresses/{addressId}` - Update address details
    - `DELETE /api/v1/addresses/{addressId}` - Delete address and remove from user
  - Added `AddressDTO` (request/response DTO) with fields: `addressId`, `street`, `city`, `state`, `country`, `postalCode`
  - Implemented `AddressService` interface and `AddressServiceImpl` with full CRUD logic:
    - `createAddress()` - Associates new address with current user, validates via `@Valid` annotation
    - `getAddresses()` - Returns all addresses from database
    - `getAddressById()` - Fetches specific address, throws `ResourceNotFoundException` if not found
    - `getUserAddresses()` - Retrieves all addresses associated with current authenticated user
    - `updateAddressById()` - Updates address fields and maintains user-address relationship
    - `deleteAddressById()` - Removes address and cleans up user address list
  - Enhanced `Address` model relationships:
    - `@ManyToOne` relationship with `User` for address ownership
    - Proper cascade and fetch strategies configured
  - Updated `User` model:
    - `@OneToMany` relationship to `Address` with proper cascade handling
    - Supports multi-address management per user (for billing, shipping, etc.)
  - Leveraged `AuthUtil` for user context:
    - `authUtil.getLoggedInUser()` retrieves currently authenticated user
    - Ensures all address operations are user-scoped
  - Used `ObjectMapper` for automatic DTO/Entity conversions
  - Input validation on all address fields:
    - `@NotBlank` for required fields
    - `@Size` constraints with custom error messages
  - Files created/modified:
    - New: `AddressController.java`, `AddressService.java`, `AddressServiceImpl.java`
    - New: `AddressDTO.java`
    - New: `AddressRepository.java`
    - Modified: `Address.java` (enhanced relationships)
    - Modified: `User.java` (added OneToMany relationship to Address)
  - Benefits:
    - ✅ Full address lifecycle management (Create, Read, Update, Delete)
    - ✅ Multi-address support for users (billing, shipping, home, work, etc.)
    - ✅ User-scoped address retrieval ensures data privacy
    - ✅ Input validation prevents data quality issues
    - ✅ Foundation for order/checkout flow to use saved addresses
    - ✅ Complete RESTful API for address operations

**Previous Updates (March 16, 2026):**
- 🛒 **Cart Feature Expanded Beyond Initial Scaffold**
  - Added cart retrieval endpoints:
    - `GET /api/v1/cart/allUserCarts` - returns all carts
    - `GET /api/v1/cart/users/cart` - returns logged-in user's cart
  - Added cart quantity update endpoint:
    - `PUT /api/v1/cart/products/{productId}/quantity/{operation}`
    - Uses operation values like `add` (+1) or `delete` (-1)
  - Added cart item deletion endpoint:
    - `DELETE /api/v1/cart/{cartId}/products/{productId}`
  - Expanded cart service logic to support:
    - add/remove quantity changes
    - remove item when quantity reaches zero
    - cart total recalculation after updates/deletes
- 🔧 **Repository and service support for richer cart workflows**
  - Added cart lookup query methods in `CartRepository` (`findCartByEmail`, `findCartByEmailAndCartId`)
  - Added delete query in `CartItemRepository` for product removal from a specific cart
  - Added `AuthUtil`-driven user context usage in cart flow for user-specific operations

**Previous Updates (March 10, 2026):**
- 🍪 **JWT Cookie-Based Authentication Flow Update**
  - Updated `JwtUtils` to support cookie-based JWT handling:
    - Reads token from cookie via `getJwtFromCookie(HttpServletRequest request)`
    - Generates auth cookie via `generateJwtCookie(UserDetailsImpl userDetails)`
    - Added logout helper `getCleanJwtCookie()`
  - Added `spring.ecom.app.jwtCookieName=sb-ecomm-jwt` in `application.properties`
  - Updated `AuthTokenFilter` to parse JWT from cookie instead of Authorization header
- 🔐 **Authentication Controller Endpoint Expansion (`AuthController`)**
  - Added `GET /api/v1/auth/current-user` to return authenticated username
  - Added `GET /api/v1/auth/user` to return authenticated user details + roles
  - Added `POST /api/v1/auth/signout` to clear the JWT cookie
  - Updated `POST /api/v1/auth/signin` to set `Set-Cookie` header and return `UserLoginResponse`
- 🛒 **Cart Feature Foundation (In Progress)**
  - Added cart domain models: `Cart`, `CartItem`
  - Added cart repositories: `CartRepository`, `CartItemRepository`
  - Added cart DTOs: `CartDTO`, `CartItemDTO`
  - Added `CartService` + `CartServiceImpl` with initial add-to-cart flow
  - Added cart endpoint scaffold: `POST /api/v1/cart/products/{productId}/quantity/{quantity}`
  - Updated `User` and `Product` relationships for cart integration

**Previous Updates (March 9, 2026):**
- 🔐 **Authentication API + Security Configuration Completion**
  - Added `AuthController` with two authentication endpoints under `/api/v1/auth`:
    - `POST /api/v1/auth/signup` - Registers a new user with optional role assignment
    - `POST /api/v1/auth/signin` - Authenticates user credentials and returns JWT + role list
  - Added `WebSecurityConfig` with:
    - `SecurityFilterChain` + stateless session policy
    - `DaoAuthenticationProvider` wired to `UserDetailsServiceImpl`
    - `BCryptPasswordEncoder` bean for password hashing
    - JWT filter integration (`AuthTokenFilter`) before `UsernamePasswordAuthenticationFilter`
    - Public access matchers for `/api/v1/auth/**`, Swagger/OpenAPI docs, H2 console, `/api/v1/test/**`, and `/images/**`
    - All other routes currently require authentication via `.anyRequest().authenticated()`
  - Added startup seed logic (`CommandLineRunner`) to create default roles and bootstrap users:
    - Users: `user1`, `seller1`, `admin`
    - Roles are assigned/updated on startup if missing
  - Added request/response DTOs for auth flow:
    - `SignupRequest` (`username`, `email`, `password`, `role`)
    - `LoginRequest` moved to `security/request`
    - `UserLoginResponse` moved to `security/response`
    - `MessageResponse` for standardized success/error messaging
  - Added `RoleRepository` with `findByRoleName(AppRole)` to support role resolution during signup
  - Added duplicate checks during registration using `UserRepository.existsByUserName()` and `UserRepository.existsByEmail()`
  - Refactored model package ownership:
    - `Address`, `Role`, and `AppRole` moved from `db/model` to `user/model`

**Previous Updates (March 5, 2026):**
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
  - ⚠️ Bearer token authentication in request headers (**legacy approach, no longer used by current flow**)
  - ✅ Cookie-based JWT authentication (`Set-Cookie` on signin + cookie parsing in filter)
  - ✅ AuthEntryPointJwt for standardized JSON error responses (401 UNAUTHORIZED)
  - ✅ Comprehensive input validation on user credentials (username, email, password)
  - ✅ Unique constraints on username and email to prevent duplicates
  - ✅ Debug logging for authentication and authorization troubleshooting
  - ✅ Password-based authentication with JWT token generation
  - ✅ User registration endpoint (`POST /api/v1/auth/signup`)
  - ✅ User login endpoint (`POST /api/v1/auth/signin`)
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
├── cart/                         # Cart Feature Slice
│   ├── controller/               # 🚧 Endpoint scaffolding in progress
│   │   └── CartController.java
│   ├── service/                  # 🚧 Business logic in progress
│   │   ├── CartService.java
│   │   └── CartServiceImpl.java
│   ├── repository/               # ✅ Data access layer (JPA/Database)
│   │   ├── CartRepository.java
│   │   └── CartItemRepository.java
│   ├── model/                    # ✅ Domain entities
│   │   ├── Cart.java
│   │   └── CartItem.java
│   └── dto/                      # ✅ Data transfer objects
│       └── request/
│           ├── CartDTO.java
│           └── CartItemDTO.java
│
├── user/                         # User Management Feature Slice
│   ├── controller/               # 🚧 REST endpoints for profile/account management (planned)
│   ├── service/                  # 🚧 Business logic layer (planned)
│   ├── repository/               # ✅ Data access layer (JPA/Database)
│   │   ├── UserRepository.java (findByUserName, existsByUserName, existsByEmail)
│   │   └── RoleRepository.java (findByRoleName)
│   ├── model/                    # ✅ Domain entities (JPA Entities)
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── AppRole.java
│   │   └── Address.java
│   ├── dto/                      # 🚧 Data transfer objects (planned)
│   ├── exception/                # 🚧 Feature-specific exceptions (planned)
│   ├── validator/                # 🚧 Custom validation logic (planned)
│   └── mapper/                   # 🚧 DTO/Entity mappers (planned)
│
├── security/                     # Security & Authentication Feature Slice
│   ├── config/                   # ✅ Security configuration
│   │   └── WebSecurityConfig.java
│   ├── controller/               # ✅ Authentication controller
│   │   └── AuthController.java
│   ├── jwt/                      # ✅ JWT token handling
│   │   ├── JwtUtils.java
│   │   ├── AuthTokenFilter.java
│   │   └── AuthEntryPointJwt.java
│   ├── request/                  # ✅ Authentication request DTOs
│   │   ├── LoginRequest.java
│   │   └── SignupRequest.java
│   ├── response/                 # ✅ Authentication response DTOs
│   │   ├── UserLoginResponse.java
│   │   └── MessageResponse.java
│   └── services/                 # ✅ Spring Security integration
│       ├── UserDetailsServiceImpl.java
│       └── UserDetailsImpl.java
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

> **Security Note:** With the current `WebSecurityConfig`, endpoints outside `/api/v1/auth/**`, docs, H2 console, `/api/v1/test/**`, and `/images/**` require authentication.

### Authentication

**Register User**
```
POST /api/v1/auth/signup
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "role": ["user"]
}
```
Creates a new user account, checks duplicate username/email, hashes password with BCrypt, and assigns roles.

**Response:** `200 OK`
```json
{
  "message": "User registered successfully!"
}
```

**Response (Duplicate Username/Email):** `400 BAD REQUEST`
```json
{
  "message": "Error: Username is already taken!"
}
```

**Authenticate User**
```
POST /api/v1/auth/signin
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123"
}
```
Authenticates credentials, returns roles, and sets JWT in an HTTP cookie (`Set-Cookie`).

**Response:** `200 OK`
```json
{
  "id": 1,
  "jwt": "sb-ecomm-jwt=<token>; Path=/api/v1; Max-Age=86400",
  "username": "newuser",
  "roles": ["ROLE_USER"]
}
```

**Response (Invalid Credentials):** `401 UNAUTHORIZED`
```json
{
  "message": "Invalid username or password",
  "isSuccessful": false
}
```

**Get Current Username**
```
GET /api/v1/auth/current-user
```
Returns the username from the current authenticated principal.

**Get Current User Details**
```
GET /api/v1/auth/user
```
Returns current user id, username, and role list.

**Sign Out User**
```
POST /api/v1/auth/signout
```
Clears the JWT cookie and signs the user out.

### Cart Management (Work in Progress)

**Add Product to Cart**
```
POST /api/v1/cart/products/{productId}/quantity/{quantity}
```
Adds a product to the authenticated user's cart and creates a cart automatically if one does not already exist.

**Get All User Carts**
```
GET /api/v1/cart/allUserCarts
```
Returns all carts currently stored in the system.

**Get Logged-In User Cart**
```
GET /api/v1/cart/users/cart
```
Returns the active cart for the authenticated user.

**Update Product Quantity in Cart**
```
PUT /api/v1/cart/products/{productId}/quantity/{operation}
```
Updates quantity by operation keyword:
- `add` increases quantity by 1
- `delete` decreases quantity by 1 (removes item when quantity becomes 0)

**Delete Product from Cart**
```
DELETE /api/v1/cart/{cartId}/products/{productId}
```
Removes a specific product from the given cart.

### Address Management

**Create Address for Current User**
```
POST /api/v1/addresses
Content-Type: application/json

{
  "street": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001"
}
```
Creates a new address for the currently authenticated user. Address ID is auto-generated.

**Validation Rules:**
- `street` is required, minimum 5 characters
- `city` is required, minimum 2 characters
- `state` is required, minimum 2 characters
- `country` is required, minimum 2 characters
- `postalCode` is required, minimum 5 characters
- Uses `@Valid` annotation for input validation

**Response:** `201 CREATED`
```json
{
  "addressId": 1,
  "street": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001"
}
```

**Response (Validation Error):** `400 BAD REQUEST`
```json
{
  "timestamp": "2026-03-18T12:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Street must be at least 5 characters",
  "path": "/api/v1/addresses"
}
```

**Get All Addresses in System**
```
GET /api/v1/addresses
```
Returns all addresses from the database (admin/system view).

**Response:** `200 OK`
```json
[
  {
    "addressId": 1,
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "country": "USA",
    "postalCode": "10001"
  },
  {
    "addressId": 2,
    "street": "456 Oak Avenue",
    "city": "Los Angeles",
    "state": "CA",
    "country": "USA",
    "postalCode": "90001"
  }
]
```

**Get Address by ID**
```
GET /api/v1/addresses/{addressId}
```
Retrieves a specific address by its ID.

**Response:** `302 FOUND`
```json
{
  "addressId": 1,
  "street": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001"
}
```

**Response (Not Found):** `404 NOT FOUND`
```json
{
  "message": "Address not found with addressId: 1",
  "isSuccess": false
}
```

**Get All Addresses for Current User**
```
GET /api/v1/addresses/user/addresses
```
Returns all addresses associated with the currently authenticated user.

**Response:** `200 OK`
```json
[
  {
    "addressId": 1,
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "country": "USA",
    "postalCode": "10001"
  }
]
```

**Update Address**
```
PUT /api/v1/addresses/{addressId}
Content-Type: application/json

{
  "street": "456 Oak Avenue",
  "city": "Los Angeles",
  "state": "CA",
  "country": "USA",
  "postalCode": "90001"
}
```
Updates an existing address by ID and maintains user-address relationship.

**Response:** `200 OK`
```json
{
  "addressId": 1,
  "street": "456 Oak Avenue",
  "city": "Los Angeles",
  "state": "CA",
  "country": "USA",
  "postalCode": "90001"
}
```

**Delete Address**
```
DELETE /api/v1/addresses/{addressId}
```
Deletes an address by ID and removes it from the user's address list.

**Response:** `200 OK`
```json
{
  "addressId": 1,
  "street": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001"
}
```

**Response (Not Found):** `404 NOT FOUND`
```json
{
  "message": "Address not found with addressId: 1",
  "isSuccess": false
}
```

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

#### Cookie-based auth (current)

```bash
# Sign in and store auth cookie
curl -s -c cookies.txt -X POST http://localhost:8080/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password1"}'

# Get current authenticated user
curl -s -b cookies.txt http://localhost:8080/api/v1/auth/user

# Get all categories
curl -s "http://localhost:8080/api/v1/public/categories?pageNumber=0&pageSize=10&sortBy=categoryId&sortOrder=asc" \
  -b cookies.txt

# Create a category
curl -s -X POST http://localhost:8080/api/v1/public/categories \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"categoryName": "Electronics"}'

# Add a product to a category
curl -s -X POST http://localhost:8080/api/v1/admin/categories/1/product \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"productName": "iPhone 15", "quantity": 50, "price": 999.99, "discount": 10, "description": "Latest iPhone model"}'

# Get all products
curl -s http://localhost:8080/api/v1/public/products \
  -b cookies.txt

# Add product to cart (WIP endpoint)
curl -s -X POST http://localhost:8080/api/v1/cart/products/1/quantity/2 \
  -b cookies.txt

# Get logged-in user's cart
curl -s http://localhost:8080/api/v1/cart/users/cart \
  -b cookies.txt

# Increase quantity of product 1 in cart by 1
curl -s -X PUT http://localhost:8080/api/v1/cart/products/1/quantity/add \
  -b cookies.txt

# Decrease quantity of product 1 in cart by 1
curl -s -X PUT http://localhost:8080/api/v1/cart/products/1/quantity/delete \
  -b cookies.txt

# Remove product 1 from cart 1
curl -s -X DELETE http://localhost:8080/api/v1/cart/1/products/1 \
  -b cookies.txt

# Create an address for current user
curl -s -X POST http://localhost:8080/api/v1/addresses \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"street": "123 Main Street", "city": "New York", "state": "NY", "country": "USA", "postalCode": "10001"}'

# Get all addresses in system
curl -s http://localhost:8080/api/v1/addresses \
  -b cookies.txt

# Get current user's addresses
curl -s http://localhost:8080/api/v1/addresses/user/addresses \
  -b cookies.txt

# Get specific address by ID
curl -s http://localhost:8080/api/v1/addresses/1 \
  -b cookies.txt

# Update address
curl -s -X PUT http://localhost:8080/api/v1/addresses/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"street": "456 Oak Avenue", "city": "Los Angeles", "state": "CA", "country": "USA", "postalCode": "90001"}'

# Delete address
curl -s -X DELETE http://localhost:8080/api/v1/addresses/1 \
  -b cookies.txt

# Sign out (clears cookie)
curl -s -X POST http://localhost:8080/api/v1/auth/signout \
  -b cookies.txt
```

#### Bearer-token auth (legacy, no longer used by current flow)

```bash
# Legacy sign in and capture JWT from response body (kept for reference only)
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password1"}' | jq -r '.jwt')

# Legacy: get all categories with Authorization header
curl "http://localhost:8080/api/v1/public/categories?pageNumber=0&pageSize=10&sortBy=categoryId&sortOrder=asc" \
  -H "Authorization: Bearer $TOKEN"

# Legacy: create a category with Authorization header
curl -X POST http://localhost:8080/api/v1/public/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"categoryName": "Electronics"}'

# Legacy: add a product to a category with Authorization header
curl -X POST http://localhost:8080/api/v1/admin/categories/1/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"productName": "iPhone 15", "quantity": 50, "price": 999.99, "discount": 10}'

# Legacy: get all products with Authorization header
curl http://localhost:8080/api/v1/public/products \
  -H "Authorization: Bearer $TOKEN"

# Legacy: get products by category with Authorization header
curl http://localhost:8080/api/v1/public/categories/1/products \
  -H "Authorization: Bearer $TOKEN"

# Legacy: search products by keyword with Authorization header
curl http://localhost:8080/api/v1/public/products/keyword/iphone \
  -H "Authorization: Bearer $TOKEN"

# Legacy: update a product with Authorization header
curl -X PUT http://localhost:8080/api/v1/admin/products/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"productName": "iPhone 15 Pro", "description": "Latest iPhone Pro model", "quantity": 75, "price": 1199.99, "discount": 15}'

# Legacy: delete a product with Authorization header
curl -X DELETE http://localhost:8080/api/v1/admin/products/1 \
  -H "Authorization: Bearer $TOKEN"

# Legacy: update a category with Authorization header
curl -X PUT http://localhost:8080/api/v1/public/categories/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"categoryName": "Updated Electronics"}'

# Legacy: delete a category with Authorization header
curl -X DELETE http://localhost:8080/api/v1/admin/categories/1 \
  -H "Authorization: Bearer $TOKEN"
```
