package com.echapps.ecom.project.user.address.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    @Schema(description = "Unique identifier for the address", example = "1")
    private Long addressId;

    @Schema(description = "Street address of the user", example = "123 Main St")
    private String street;

    @Schema(description = "City of the user's address", example = "New York")
    private String city;

    @Schema(description = "State or province of the user's address", example = "NY")
    private String state;

    @Schema(description = "Country of the user's address", example = "US")
    private String country;

    @Schema(description = "Postal code of the user's address", example = "10001")
    private String postalCode;


}
