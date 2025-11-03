package com.example.ecoms.Service;

import java.util.List;
import java.util.Optional;

import com.example.ecoms.Entity.User;

public interface UserService {

    
    User registerUser(User user);

    User loginUser(String email, String password);

    // Retrieve all registered users
    List<User> getAllUsers();

    
    Optional<User> findByEmail(String email);

    
    void deleteUser(Long userId);
}
