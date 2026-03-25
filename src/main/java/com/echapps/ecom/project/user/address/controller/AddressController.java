package com.echapps.ecom.project.user.address.controller;

import com.echapps.ecom.project.user.address.dto.request.AddressDTO;
import com.echapps.ecom.project.user.address.service.AddressService;
import com.echapps.ecom.project.user.model.User;
import com.echapps.ecom.project.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AddressController {

    private final AddressService addressService;
    private final AuthUtil authUtil;

    public AddressController(AddressService addressService, AuthUtil authUtil) {
        this.addressService = addressService;
        this.authUtil = authUtil;
    }

    @PostMapping("/addresses")
    @Tag(name = "Address APIs", description = "APIs for managing user addresses")
    @Operation(summary = "Create a new address", description = "Create a new address for the currently logged-in user by providing the address details in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid address data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User currentUser = authUtil.getLoggedInUser();
        AddressDTO addressToSave = addressService.createAddress(addressDTO, currentUser);
        return new ResponseEntity<>(addressToSave, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    @Tag(name = "Address APIs", description = "APIs for managing user addresses")
    @Operation(summary = "Get all addresses", description = "Retrieve a list of all addresses in the system. This endpoint is typically used for administrative purposes to view all user addresses.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all addresses"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<AddressDTO>> getAddresses() {
        List<AddressDTO> addresses = addressService.getAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    @Operation(summary = "Get an address by ID", description = "Retrieve the details of an address by providing its ID in the path variable. This will return the address information if it exists.")
    @Tag(name = "Address APIs", description = "APIs for managing user addresses")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Address found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Address not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<AddressDTO> getAddressById(@Parameter(description = "Id of address to get") @PathVariable Long addressId) {
        AddressDTO address = addressService.getAddressById(addressId);
        return new ResponseEntity<>(address, HttpStatus.FOUND);
    }

    @GetMapping("/user/addresses")
    @Tag(name = "Address APIs", description = "APIs for managing user addresses")
    @Operation(summary = "Get addresses for the current user", description = "Retrieve a list of addresses associated with the currently logged-in user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved addresses for the current user"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<AddressDTO>> getAddressesForCurrentUser() {
        User currentUser = authUtil.getLoggedInUser();
        List<AddressDTO> addressDTOList = addressService.getUserAddresses(currentUser);
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    @Tag(name = "Address APIs", description = "APIs for managing user addresses")
    @Operation(summary = "Update an address by ID", description = "Update an existing address by providing its ID in the path variable and the updated address details in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid address data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Address not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<AddressDTO> updateAddressById(@Parameter(description = "Id of address to update") @PathVariable Long addressId,
                                                        @RequestBody AddressDTO addressDTO) {
        AddressDTO addressToUpdate = addressService.updateAddressById(addressId, addressDTO);
        return new ResponseEntity<>(addressToUpdate, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    @Tag(name = "Address APIs", description = "APIs for managing user addresses")
    @Operation(summary = "Delete an address by ID", description = "Delete an address by providing its ID in the path variable. This will remove the address from the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Address not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<AddressDTO> deleteAddressById(@Parameter(description = "Id of address to delete") @PathVariable Long addressId) {
        AddressDTO deletedAddress = addressService.deleteAddressById(addressId);
        return new ResponseEntity<>(deletedAddress, HttpStatus.OK);
    }
}
