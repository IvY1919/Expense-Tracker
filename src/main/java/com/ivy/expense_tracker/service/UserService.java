package com.ivy.expense_tracker.service;
import com.ivy.expense_tracker.database.UserEntity;
import com.ivy.expense_tracker.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService( UserRepository userRepository,
        PasswordEncoder passwordEncoder){
        
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        }

    // For Sign Up 
    public UserEntity registerUser(String username, String email, String password){
        if(userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists.");
        }

        UserEntity user = new UserEntity();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);

    }

    
}
