package com.echapps.ecom.project.security.controller;

import com.echapps.ecom.project.security.jwt.JwtUtils;
import com.echapps.ecom.project.security.request.LoginRequest;
import com.echapps.ecom.project.security.request.SignupRequest;
import com.echapps.ecom.project.security.response.MessageResponse;
import com.echapps.ecom.project.security.response.LoginResponse;
import com.echapps.ecom.project.security.services.UserDetailsImpl;
import com.echapps.ecom.project.user.model.AppRole;
import com.echapps.ecom.project.user.model.Role;
import com.echapps.ecom.project.user.model.User;
import com.echapps.ecom.project.user.repository.RoleRepository;
import com.echapps.ecom.project.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

     // This controller will handle authentication-related endpoints such as login and registration.
     // You can implement methods for user registration, login, and token generation here.

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/signup")
    @Tag(name = "Authentication APIs", description = "APIs for user authentication and session management")
    @Operation(summary = "Register a new user", description = "Create a new user account by providing the username, email, password, and optional roles in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data or username/email already taken", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUserName(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already taken!"));
        }

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()));

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch(role) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/current-user")
    @Tag(name = "Authentication APIs", description = "APIs for user authentication and session management")
    @Operation(summary = "Get current logged-in user's username", description = "Retrieve the username of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current user's username"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user is not authenticated", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public String getCurrentUserName(Authentication authentication) {
        if (authentication != null) {
            return authentication.getName();
        } else {
            return "";
        }
    }

    @GetMapping("/user")
    @Tag(name = "Authentication APIs", description = "APIs for user authentication and session management")
    @Operation(summary = "Get current logged-in user's details", description = "Retrieve the details of the currently authenticated user, including their ID, username, and roles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current user's details"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user is not authenticated", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<LoginResponse> getUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        LoginResponse loginResponse = new LoginResponse(userDetails.getId(), userDetails.getUsername(), roles);
        return ResponseEntity
                .ok()
                .body(loginResponse);
    }

    @PostMapping("/signin")
    @Tag(name = "Authentication APIs", description = "APIs for user authentication and session management")
    @Operation(summary = "Authenticate user and generate JWT token", description = "Authenticate a user by providing their username and password in the request body. If authentication is successful, a JWT token will be generated and returned in the response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully and JWT token generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid username or password", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = null;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Invalid username or password");
            map.put("isSuccessful", false);

            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        LoginResponse loginResponse = new LoginResponse(userDetails.getId(), jwtCookie.toString(), userDetails.getUsername(), roles);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/signout")
    @Tag(name = "Authentication APIs", description = "APIs for user authentication and session management")
    @Operation(summary = "Logout user and clear JWT token", description = "Logout the currently authenticated user by clearing the JWT token from the response cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User logged out successfully and JWT token cleared"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user is not authenticated", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

}
