package com.ivy.expense_tracker.service;

import org.springframework.stereotype.Service;

import com.ivy.expense_tracker.repository.ExpenseRepository;
import com.ivy.expense_tracker.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import com.ivy.expense_tracker.database.ExpenseEntity;
import com.ivy.expense_tracker.database.UserEntity;


@Service
public class ExpenseService {
    
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    
    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository){
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    // public List<ExpenseEntity> getAllExpenses(){
    //     return expenseRepository.findAll();
    // }

    // Get total expense for current month(dashboard)
    public BigDecimal getCurrentMonthTotal(String email){
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal total = expenseRepository.getMonthlyTotal(email, startDate, endDate);

        return total == null ? BigDecimal.ZERO : total;
    }

    public BigDecimal getMonthlyTotal(String email, LocalDate startDate, LocalDate endDate){
        BigDecimal total = expenseRepository.getMonthlyTotal( email, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Get monthly totals for a specific year
    public List<BigDecimal> getMonthlyTotals(String email, int year){
        LocalDate startDate = LocalDate.of(year,1,1);
        LocalDate endDate = LocalDate.of(year,12,31);

        List<Object[]> results = expenseRepository.getMonthlyTotals(email,startDate,endDate);
        List<BigDecimal> monthlyTotals = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

        for (Object[] result : results) {
        // DATE_TRUNC returns a date/time value
        LocalDate monthDate =(LocalDate) result[0];
        BigDecimal total = (BigDecimal) result[1];
        int monthIndex = monthDate.getMonthValue() - 1;
        monthlyTotals.set(monthIndex, total);   }
        
        return monthlyTotals;
    }

    // Get yearly totals
    public List<BigDecimal> getYearlyTotals(String email, int currentYear){

        int startYear = currentYear - 4;
        int endYear = currentYear + 2;

        LocalDate startDate = LocalDate.of(startYear, 1, 1);
        LocalDate endDate = LocalDate.of(endYear, 12, 31);

        List<Object[]> results = expenseRepository.getYearlyTotals( email, startDate, endDate );

        List<BigDecimal> yearlyTotals = new ArrayList<>(Collections.nCopies(7, BigDecimal.ZERO) );

        for (Object[] result : results) {

            LocalDate yearDate = (LocalDate) result[0];
            BigDecimal total = (BigDecimal) result[1];
            int yearIndex = yearDate.getYear() - startYear;
            yearlyTotals.set(yearIndex, total);
        }
        return yearlyTotals;
    }

    public List<ExpenseEntity> getExpensesByUserIdAndDate( String email,LocalDate startDate, LocalDate endDate) {
        return expenseRepository.getExpensesByUserIdAndDate(email, startDate, endDate);
    }


    public void updateExpense(Long expId,
                                LocalDate expDate,
                                String description,
                                BigDecimal expAmount,
                                String paymentMethod,
                                String email) {
        ExpenseEntity expense = expenseRepository.findByExpIdAndUserIdEmail(expId, email)
                        .orElseThrow(() -> new RuntimeException("Expense not found"));
        expense.setExpDate(expDate);
        expense.setDescription(description);
        expense.setExpAmount(expAmount);
        expense.setPaymentMethod(paymentMethod);
        expenseRepository.save(expense);
    }

    public void deleteExpense(Long expId, String email) {
        ExpenseEntity expense = expenseRepository.findByExpIdAndUserIdEmail(expId, email)
                        .orElseThrow(() ->new RuntimeException("Expense not found"));
        expenseRepository.delete(expense);
    }

    public void addExpense(ExpenseEntity expense, String email){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        expense.setUserId(user);
        expenseRepository.save(expense);

    }
// for donutchart
    public String getCategory(String description){
        if (description == null || description.isBlank()) {
            return "Other";
        }

        String desc = description.toLowerCase();

        if (   desc.contains("food")
            || desc.contains("lunch")
            || desc.contains("dinner")
            || desc.contains("lunch")
            || desc.contains("snack")
            || desc.contains("coffee")
            || desc.contains("cafe")
            || desc.contains("breakfast")
            || desc.contains("eat")){

                return "Food";
        }

        if(    desc.contains("plane")
            || desc.contains("train")
            || desc.contains("bus")
            || desc.contains("gasoline")
            || desc.contains("taxi")
            || desc.contains("transport"))
            {
                return "Transport";
            }

        if (desc.contains("movie")
                || desc.contains("game")
                || desc.contains("concert")
                || desc.contains("entertainment")) {
            return "Entertainment";
        }

        if (desc.contains("clothes")
                || desc.contains("shoes")
                || desc.contains("shopping")) {
            return "Shopping";
        }

        if (desc.contains("electric")
                || desc.contains("water")
                || desc.contains("internet")
                || desc.contains("gass")
                || desc.contains("phone")
                || desc.contains("bill")) {
            return "Bills";
        }

        if (desc.contains("rent")) {
            return "Rent";
        }

        return "Other";
    }
// for donutchart
    public Map<String, BigDecimal> getCategoryTotals( String email, LocalDate starDate, LocalDate endDate){

        List<ExpenseEntity> expenses = expenseRepository.getExpensesByUserIdAndDate(email, starDate, endDate);
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        for(ExpenseEntity expense : expenses){
            String category = getCategory(expense.getDescription());
            categoryTotals.merge(category, expense.getExpAmount(), BigDecimal::add);
        }
        return categoryTotals;
    }


}
