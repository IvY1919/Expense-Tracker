package com.ivy.expense_tracker.service;
import com.ivy.expense_tracker.database.UserEntity;
import com.ivy.expense_tracker.repository.ExpenseRepository;
import com.ivy.expense_tracker.repository.IncomeRepository;
import com.ivy.expense_tracker.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService( UserRepository userRepository,
        PasswordEncoder passwordEncoder, ExpenseRepository expenseRepository, IncomeRepository incomeRepository){
        
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
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
        user.setTargetAmount(BigDecimal.ZERO);

        return userRepository.save(user);

    }

    public void updateTargetAmount(String email, BigDecimal targetAmount){
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTargetAmount(targetAmount);
        userRepository.save(user);
    }

    public BigDecimal getTargetAmount(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (user == null) {
            return BigDecimal.ZERO;
        }
        return user.getTargetAmount();
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional
    public void deleteAccount(String email){
        UserEntity user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));
        expenseRepository.deleteByUserId(user);
        incomeRepository.deleteByUserId(user);
        userRepository.delete(user);
    }

    
}
