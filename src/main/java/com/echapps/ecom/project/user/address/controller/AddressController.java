package com.echapps.ecom.project.user.address.controller;

import com.echapps.ecom.project.user.address.dto.request.AddressDTO;
import com.echapps.ecom.project.user.address.service.AddressService;
import com.echapps.ecom.project.user.model.User;
import com.echapps.ecom.project.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;
    private final AuthUtil authUtil;

    public AddressController(AddressService addressService, AuthUtil authUtil) {
        this.addressService = addressService;
        this.authUtil = authUtil;
    }

    @PostMapping()
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User currentUser = authUtil.getLoggedInUser();
        AddressDTO addressToSave = addressService.createAddress(addressDTO, currentUser);
        return new ResponseEntity<>(addressToSave, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<List<AddressDTO>> getAddresses() {
        List<AddressDTO> addresses = addressService.getAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {
        AddressDTO address = addressService.getAddressById(addressId);
        return new ResponseEntity<>(address, HttpStatus.FOUND);
    }

    @GetMapping("/user/addresses")
    public ResponseEntity<List<AddressDTO>> getAddressesForCurrentUser() {
        User currentUser = authUtil.getLoggedInUser();
        List<AddressDTO> addressDTOList = addressService.getUserAddresses(currentUser);
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO) {
        AddressDTO addressToUpdate = addressService.updateAddressById(addressId, addressDTO);
        return new ResponseEntity<>(addressToUpdate, HttpStatus.OK);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<AddressDTO> deleteAddressById(@PathVariable Long addressId) {
        AddressDTO deletedAddress = addressService.deleteAddressById(addressId);
        return new ResponseEntity<>(deletedAddress, HttpStatus.OK);
    }
}
