package com.echapps.ecom.project.security.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {

    @NotBlank
    @Schema(description = "Username of the user trying to log in", example = "john_doe")
    private String username;

    @NotBlank
    @Schema(description = "Password of the user trying to log in", example = "P@ssw0rd!")
    private String password;

}
