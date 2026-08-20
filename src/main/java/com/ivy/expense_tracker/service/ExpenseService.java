package com.ivy.expense_tracker.service;

import org.springframework.stereotype.Service;

import com.ivy.expense_tracker.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ivy.expense_tracker.database.ExpenseEntity;

@Service
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    
    public ExpenseService(ExpenseRepository expenseRepository){
        this.expenseRepository = expenseRepository;
    }

    public List<ExpenseEntity> getAllExpenses(){
        return expenseRepository.findAll();
    }

    // Get total expense for current month(dashboard)
    public BigDecimal getCurrentMonthTotal(String email){
        LocalDate today = LocalDate.now();
        LocalDate starDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal total = expenseRepository.getMonthlyTotal(email, starDate, endDate);

        return total == null ? BigDecimal.ZERO : total;
    }

    // Get monthly totals for a specific year
    public List<BigDecimal> getMonthlyTotals(String email, int year){
        LocalDate starDate = LocalDate.of(year,1,1);
        LocalDate endDate = LocalDate.of(year,12,31);

        List<Object[]> results = expenseRepository.getMonthlyTotals(email,starDate,endDate);
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

    // for table
    // public List<ExpenseEntity> getExpensesByUserId(String email) {
    //     return expenseRepository.findByUserIdEmail(email);
    // }
    // public List<ExpenseEntity> getExpensesByMonth(String email, YearMonth yearMonth) {
    //     LocalDate startDate = yearMonth.atDay(1);
    //     LocalDate endDate = yearMonth.atEndOfMonth();
    //     return expenseRepository.findByUserIdEmailAndExpDateBetween(email, startDate,endDate);
    // }

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


}
