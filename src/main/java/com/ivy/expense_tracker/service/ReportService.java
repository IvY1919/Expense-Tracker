package com.ivy.expense_tracker.service;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ivy.expense_tracker.database.ExpenseEntity;
import com.ivy.expense_tracker.database.IncomeEntity;
import com.ivy.expense_tracker.dto.TransactionDto;
import com.ivy.expense_tracker.repository.ExpenseRepository;
import com.ivy.expense_tracker.repository.IncomeRepository;


@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public ReportService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository){
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
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

    public List<TransactionDto> getLatestTransactions(String email){
        List<TransactionDto> trans = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<ExpenseEntity> expenses = expenseRepository.getExpensesByUserIdAndDate(email, startDate, endDate);
        List<IncomeEntity> incomes = incomeRepository.getIncomeByUserIdANDDate(email, startDate, endDate);

        for(ExpenseEntity expense : expenses){
            trans.add(new TransactionDto(expense.getExpDate(), expense.getDescription(),"Expense", expense.getExpAmount()));
        }

        for(IncomeEntity income : incomes){
            trans.add(new TransactionDto(income.getIncomeDate(), income.getDescription(),"Income", income.getIncomeAmount()));
        }

        trans.sort(Comparator.comparing(TransactionDto::getDate).reversed());

        return trans.stream()
            .limit(10)
            .toList();

    }

    public List<TransactionDto> getAllTransactions(String email){

        List<TransactionDto> trans = new ArrayList<>();
        List<ExpenseEntity> expenses = expenseRepository.findByUserIdEmail(email);
        List<IncomeEntity> incomes = incomeRepository.findByUserIdEmail(email);

        for(ExpenseEntity expense : expenses){
            trans.add(new TransactionDto(expense.getExpDate(), expense.getDescription(),"Expense", expense.getExpAmount()));
        }

        for(IncomeEntity income : incomes){
            trans.add(new TransactionDto(income.getIncomeDate(), income.getDescription(),"Income", income.getIncomeAmount()));
        }
        trans.sort(Comparator.comparing(TransactionDto::getDate).reversed());

        return trans;

    }

    
}
