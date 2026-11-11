package com.ivy.expense_tracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ivy.expense_tracker.database.IncomeEntity;
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

    public HashMap<String, BigDecimal> getAverageBalance(String email){
        LocalDate today = LocalDate.now();
        YearMonth lasMonth = YearMonth.from(today).minusMonths(1);
        YearMonth firsMonth = lasMonth.minusMonths(4);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int availableMonths = 0;

        for(int i =  0; i < 5; i++){

            YearMonth month = firsMonth.plusMonths(i);
            LocalDate starDate = month.atDay(1);
            LocalDate enDate = month.atEndOfMonth();

            BigDecimal monthlyIncome   = incomeRepository.getAverageIncomeTotal(email, starDate, enDate);
            BigDecimal monthlyExpense  = expenseRepository.getAverageExpenseTotal(email, starDate, enDate);

            if(monthlyIncome != null || monthlyExpense != null  ){
                availableMonths++;
                if (monthlyIncome != null) {
                    totalIncome = totalIncome.add(monthlyIncome);
                }
                if ( monthlyExpense != null) {
                    totalExpense = totalExpense.add(monthlyExpense);
                }
            }
        }
        BigDecimal averageIncome = BigDecimal.ZERO;
        BigDecimal averageExpense = BigDecimal.ZERO;
        BigDecimal averageSaving = BigDecimal.ZERO;
        if(availableMonths > 0){
            BigDecimal divisor = BigDecimal.valueOf(availableMonths);
            averageIncome = totalIncome.divide(divisor, 2, RoundingMode.HALF_UP);
            averageExpense = totalExpense.divide(divisor, 2, RoundingMode.HALF_UP);
            averageSaving = averageIncome.subtract(averageExpense);
        }

        HashMap<String, BigDecimal> result = new HashMap<>();
        result.put("averageIncome", averageIncome);
        result.put("averageExpense", averageExpense);
        result.put("averageSaving", averageSaving);
        result.put("availableMonths", BigDecimal.valueOf(availableMonths));       
        
        return result;
    }

    public BigDecimal savingPercentage(BigDecimal totalSaving, BigDecimal goalAmount){

        if (goalAmount == null || goalAmount.compareTo(BigDecimal.ZERO) <= 0) {
        return BigDecimal.ZERO;
        }

        if (totalSaving == null || totalSaving.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal percentage = totalSaving
        .multiply(BigDecimal.valueOf(100))
        .divide(goalAmount, 2, RoundingMode.HALF_UP);

        // Maximum progress is 100%
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BigDecimal.valueOf(100);
        }
        return percentage;

    }

}
