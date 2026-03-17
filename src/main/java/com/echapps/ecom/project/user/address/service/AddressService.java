package com.echapps.ecom.project.user.address.service;

import com.echapps.ecom.project.user.address.dto.request.AddressDTO;
import com.echapps.ecom.project.user.model.User;

public interface AddressService {
        AddressDTO createAddress(AddressDTO addressDTO, User currentUser);
}
