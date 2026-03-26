package com.echapps.ecom.project.security.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    @Schema(description = "Username of the new user", example = "john_doe")
    private String username;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Email address of the new user", example = "john@aol.com")
    private String email;

    @Schema(description = "Set of roles to assign to the new user", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    @Schema(description = "Password for the new user", example = "myP@ss!")
    private String password;


}
