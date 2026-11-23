package org.example.service;

import java.util.List;
import org.example.model.entity.Address;
import org.example.model.entity.User;
import org.example.service.dto.AddressRequest;

/** Manages the address book of a customer. */
public class AddressService {

    private final UserService userService;
    private final IdGenerator idGenerator = new IdGenerator("A");

    public AddressService(UserService userService) {
        this.userService = userService;
    }

    public List<Address> getAddresses(String userId) {
        return userService.getUser(userId).getAddresses();
    }

    public Address addAddress(String userId, AddressRequest request) {
        User user = userService.getUser(userId);
        Address address = new Address(idGenerator.next(), request.title(), request.toShippingAddress(),
                request.makeDefault());
        user.addAddress(address);
        return address;
    }

    public Address updateAddress(String userId, String addressId, AddressRequest request) {
        User user = userService.getUser(userId);
        Address address = user.getAddress(addressId);
        address.update(request.title(), request.toShippingAddress());
        if (request.makeDefault()) {
            user.setDefaultAddress(addressId);
        }
        return address;
    }

    public void deleteAddress(String userId, String addressId) {
        userService.getUser(userId).removeAddress(addressId);
    }

    public void setDefaultAddress(String userId, String addressId) {
        userService.getUser(userId).setDefaultAddress(addressId);
    }
}
