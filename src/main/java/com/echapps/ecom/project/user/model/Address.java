package com.echapps.ecom.project.user.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street must be at least 5 characters long")
    private String street;

    @NotBlank
    @Size(min = 2, message = "City must be at least 2 characters long")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State must be at least 2 characters long")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country must be at least 2 characters long")
    private String country;

    @NotBlank
    @Size(min = 5, message = "Postal code must be at least 5 characters long")
    private String postalCode;

    @ManyToMany(mappedBy = "addresses")
    @ToString.Exclude
    private List<User> users = new ArrayList<>();

    public Address(String street, String city, String state, String country, String postalCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
    }
}
