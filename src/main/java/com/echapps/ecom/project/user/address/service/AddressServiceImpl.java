package com.echapps.ecom.project.user.address.service;

import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.user.address.dto.request.AddressDTO;
import com.echapps.ecom.project.user.model.Address;
import com.echapps.ecom.project.user.model.User;
import com.echapps.ecom.project.user.repository.AddressRepository;
import com.echapps.ecom.project.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final ObjectMapper mapper;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(ObjectMapper mapper, AddressRepository addressRepository, UserRepository userRepository) {
        this.mapper = mapper;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
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
        return mapper.convertValue(savedAddress, AddressDTO.class);

    }

    @Override
    public List<AddressDTO> getAddresses() {
            List<Address> addresses = addressRepository.findAll();
            return addresses
                    .stream()
                    .map(address -> mapper.convertValue(address, AddressDTO.class))
                    .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address ", "addressId", addressId));

        return mapper.convertValue(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User currentUser) {
        List<Address> addresses = currentUser.getAddresses();
        return addresses
                .stream()
                .map(address -> mapper.convertValue(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
       Address addressToUpdate = addressRepository.findById(addressId)
               .orElseThrow(() -> new ResourceNotFoundException("Address ", "addressId", addressId));

        addressToUpdate.setStreet(addressDTO.getStreet());
        addressToUpdate.setCity(addressDTO.getCity());
        addressToUpdate.setState(addressDTO.getState());
        addressToUpdate.setCountry(addressDTO.getCountry());
        addressToUpdate.setPostalCode(addressDTO.getPostalCode());

        Address updatedAddress = addressRepository.save(addressToUpdate);

        User user = addressToUpdate.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);

        return mapper.convertValue(updatedAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO deleteAddressById(Long addressId) {
        Address addressToDelete = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address ", "addressId", addressId));

        User user = addressToDelete.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.delete(addressToDelete);
        return mapper.convertValue(addressToDelete, AddressDTO.class);
    }
}
