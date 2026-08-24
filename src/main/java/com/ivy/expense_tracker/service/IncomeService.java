package com.ivy.expense_tracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import com.ivy.expense_tracker.database.IncomeEntity;
import com.ivy.expense_tracker.database.UserEntity;
import com.ivy.expense_tracker.repository.IncomeRepository;
import com.ivy.expense_tracker.repository.UserRepository;

@Service
public class IncomeService {

    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository, UserRepository userRepository){
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    public void addIncome(IncomeEntity income, String email){
        UserEntity user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

        income.setUserId(user);
        incomeRepository.save(income);
    }

    public List<IncomeEntity> getIncomeByUserIdANDDate( String email,LocalDate startDate, LocalDate endDate) {
        return incomeRepository.getIncomeByUserIdANDDate(email, startDate, endDate);
    }

    public BigDecimal getCurrentincomeMonthTotal(String email, LocalDate startDate, LocalDate endDate){
        BigDecimal total = incomeRepository.getIncomeMonthlyTotal( email, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;   
    
    }

    public BigDecimal getCurrentMonthTotal(String email){
        LocalDate today = LocalDate.now();
        LocalDate starDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal total = incomeRepository.getIncomeMonthlyTotal(email, starDate, endDate);

        return total == null ? BigDecimal.ZERO : total;
    }

    public void updateIncome( Long incomeId,
                              LocalDate incomeDate,
                              String description,
                              BigDecimal incomeAmount,
                              String email ){

        IncomeEntity income = incomeRepository.findByIncomeIdAndUserIdEmail(incomeId, email).orElseThrow(() -> new RuntimeException("Expense not found"));
        income.setIncomeDate(incomeDate);
        income.setDescription(description);
        income.setIncomeAmount(incomeAmount);
        incomeRepository.save(income);

    }

    public void deleteIncome( Long incomeId, String email){
        IncomeEntity income = incomeRepository.findByIncomeIdAndUserIdEmail(incomeId, email).orElseThrow(() -> new RuntimeException("Expense not found"));
        incomeRepository.delete(income);

    }


    
}
