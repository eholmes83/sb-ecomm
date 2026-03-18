package com.echapps.ecom.project.user.address.service;

import com.echapps.ecom.project.user.address.dto.request.AddressDTO;
import com.echapps.ecom.project.user.model.User;

import java.util.List;

public interface AddressService {
        AddressDTO createAddress(AddressDTO addressDTO, User currentUser);
        List<AddressDTO> getAddresses();
        AddressDTO getAddressById(Long addressId);
        List<AddressDTO> getUserAddresses(User currentUser);
        AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO);
        AddressDTO deleteAddressById(Long addressId);
}
