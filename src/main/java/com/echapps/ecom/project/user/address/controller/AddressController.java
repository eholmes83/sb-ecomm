package com.echapps.ecom.project.user.address.controller;

import com.echapps.ecom.project.user.address.dto.request.AddressDTO;
import com.echapps.ecom.project.user.address.service.AddressService;
import com.echapps.ecom.project.user.model.User;
import com.echapps.ecom.project.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
