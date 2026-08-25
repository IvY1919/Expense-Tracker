package com.ivy.expense_tracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.ivy.expense_tracker.repository.ExpenseRepository;
import com.ivy.expense_tracker.repository.IncomeRepository;
import com.ivy.expense_tracker.repository.UserRepository;

@Service
public class ReportService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public ReportService(IncomeRepository incomeRepository, UserRepository userRepository, ExpenseRepository expenseRepository){
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }


    public BigDecimal getCurrentMontRemainingTotal(String email){

        LocalDate today = LocalDate.now();
        LocalDate starDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal incomeTotal = incomeRepository.getIncomeMonthlyTotal(email, starDate, endDate);
        BigDecimal expenseTotal = expenseRepository.getMonthlyTotal(email, starDate, endDate);
        if (incomeTotal == null) {
        incomeTotal = BigDecimal.ZERO;}

        if (expenseTotal == null) {
            expenseTotal = BigDecimal.ZERO;
        }
        
        return incomeTotal.subtract(expenseTotal);
    }



    
}
