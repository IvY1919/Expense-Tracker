package com.ivy.expense_tracker.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.ivy.expense_tracker.database.UserEntity;
import com.ivy.expense_tracker.repository.ExpenseRepository;
import com.ivy.expense_tracker.repository.IncomeRepository;
import com.ivy.expense_tracker.repository.UserRepository;

@Service 
public class SavingService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public SavingService( IncomeRepository incomeRepository, ExpenseRepository expenseRepository, UserRepository userRepository){
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public void updateSavingGoal(String email, BigDecimal goalAmount){
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setGoalAmount(goalAmount);
        userRepository.save(user);
    }

    public BigDecimal getSavingGoal(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        // if (user == null) {
        //     return BigDecimal.ZERO;
        // }
        return user.getGoalAmount();
    }

}
