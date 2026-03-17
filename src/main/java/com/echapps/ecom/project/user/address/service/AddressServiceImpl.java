package com.echapps.ecom.project.user.address.service;

import com.echapps.ecom.project.user.address.dto.request.AddressDTO;
import com.echapps.ecom.project.user.model.Address;
import com.echapps.ecom.project.user.model.User;
import com.echapps.ecom.project.user.repository.AddressRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final ObjectMapper mapper;
    private final AddressRepository addressRepository;

        public AddressServiceImpl(ObjectMapper mapper, AddressRepository addressRepository) {
            this.mapper = mapper;
            this.addressRepository = addressRepository;
        }

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User currentUser) {
        Address newAddress = mapper.convertValue(addressDTO, Address.class);

        List<Address> addressList = currentUser.getAddresses();
        addressList.add(newAddress);
        currentUser.setAddresses(addressList);

        newAddress.setUser(currentUser);
        Address savedAddress = addressRepository.save(newAddress);

        // return the saved address as a DTO
        return mapper.convertValue(addressRepository.save(newAddress), AddressDTO.class);

    }
}
