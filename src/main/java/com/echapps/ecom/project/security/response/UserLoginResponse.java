package com.echapps.ecom.project.security.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserLoginResponse {
    private Long id;
    private String jwt;
    private String username;
    private List<String> roles;

    public UserLoginResponse(Long id, String jwt, String username, List<String> roles) {
        this.id = id;
        this.jwt = jwt;
        this.username = username;
        this.roles = roles;
    }

}
