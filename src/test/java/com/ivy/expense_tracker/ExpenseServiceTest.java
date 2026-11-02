package com.ivy.expense_tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ivy.expense_tracker.database.ExpenseEntity;
import com.ivy.expense_tracker.database.UserEntity;
import com.ivy.expense_tracker.repository.ExpenseRepository;
import com.ivy.expense_tracker.repository.UserRepository;
import com.ivy.expense_tracker.service.ExpenseService;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    ExpenseRepository expenseRepository;

    @Mock
    UserRepository userRepository;

    ExpenseService service;

    @Test
    void getMonthlyTotalsTest(){

        // ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        // UserRepository userRepository = mock(UserRepository.class);
        // ExpenseService service = new ExpenseService(expenseRepository,userRepository);

        service = new ExpenseService(expenseRepository,userRepository);

        List<Object[]> testData = List.of(
            new Object[] { LocalDate.of(2026, 1, 1), new BigDecimal("10000") },
            new Object[] { LocalDate.of(2026, 3, 1), new BigDecimal("5000") },
            new Object[] { LocalDate.of(2026, 8, 1), new BigDecimal("20000") }
        );

        when(expenseRepository.getMonthlyTotals(
                "test@example.com",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        )).thenReturn(testData);

        List<BigDecimal> result =
            service.getMonthlyTotals("test@example.com", 2026);

        List<BigDecimal> expected = List.of(
            new BigDecimal("10000"), // January
            BigDecimal.ZERO,         // February
            new BigDecimal("5000"),  // March
            BigDecimal.ZERO,         // April
            BigDecimal.ZERO,         // May
            BigDecimal.ZERO,         // June
            BigDecimal.ZERO,         // July
            new BigDecimal("20000"), // August
            BigDecimal.ZERO,         // September
            BigDecimal.ZERO,         // October
            BigDecimal.ZERO,         // November
            BigDecimal.ZERO          // December
        );
        assertEquals(expected, result);
    }


    // If the repository returns an empty list, your service should return 12 months of BigDecimal.ZERO.
    @Test
    void getMonthlyTotals_NoExpense(){

        service = new ExpenseService(expenseRepository, userRepository);

        when(expenseRepository.getMonthlyTotals("test@example.com", 
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 12, 31)
        )).thenReturn(List.of());

        List<BigDecimal> expected = List.of(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );

    List<BigDecimal> result = service.getMonthlyTotals("test@example.com", 2026);
    assertEquals(expected, result);       

    }

    @Test
    void getCurrentMonthTotalTest(){
        service = new ExpenseService(expenseRepository, userRepository);

        BigDecimal testTotal = new BigDecimal("15000");

        when(expenseRepository.getMonthlyTotal("test@example.com", 
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 30)
        )).thenReturn(testTotal );

    BigDecimal result = service.getCurrentMonthTotal("test@example.com");
    assertEquals(testTotal, result);   
    }

    @Test
    void getCurrentMonthTotal_WhenTotalIsNull_ReturnsZero() {
        service = new ExpenseService(expenseRepository, userRepository);

        when(expenseRepository.getMonthlyTotal("test@example.com", 
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 30)
        )).thenReturn(null);

        BigDecimal result = service.getCurrentMonthTotal("test@example.com");
        assertEquals(BigDecimal.ZERO, result); 
    
    }

    @Test
    void getyearlyTotalsTest(){
        service = new ExpenseService(expenseRepository, userRepository);
        List<Object[]> testData = List.of(
            new Object[] { LocalDate.of(2022, 1, 1), new BigDecimal("10000") },
            new Object[] { LocalDate.of(2026, 3, 1), new BigDecimal("5000") },
            new Object[] { LocalDate.of(2028, 8, 1), new BigDecimal("20000") }
        );

        when(expenseRepository.getYearlyTotals("test@example.com",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2028, 12, 31)
        )).thenReturn(testData);

        List<BigDecimal> result = service.getYearlyTotals("test@example.com", 2026);
        List<BigDecimal> expected = List.of(
            new BigDecimal("10000"), // 2022
            BigDecimal.ZERO,         // 2023
            BigDecimal.ZERO, // 2024
            BigDecimal.ZERO,         // 2025
            new BigDecimal("5000"), // 2026
            BigDecimal.ZERO,         // 2027
            new BigDecimal("20000")  // 2028
        );
        assertEquals(expected, result);
    }
    
    @Test
    void getyearlyTotalsTest_allzero(){
        service = new ExpenseService(expenseRepository, userRepository);
        when(expenseRepository.getYearlyTotals("test@example.com",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2028, 12, 31)
        )).thenReturn(List.of());

        List<BigDecimal> result = service.getYearlyTotals("test@example.com", 2026);
        List<BigDecimal> expected = List.of(
            BigDecimal.ZERO, // 2022
            BigDecimal.ZERO,         // 2023
            BigDecimal.ZERO, // 2024
            BigDecimal.ZERO,         // 2025
            BigDecimal.ZERO, // 2026
            BigDecimal.ZERO,         // 2027
            BigDecimal.ZERO  // 2028
        );
        assertEquals(expected, result);
    }

    @Test
    void updateExpenseTest(){
        service = new ExpenseService(expenseRepository, userRepository);
        ExpenseEntity expense = new ExpenseEntity();

        expense.setExpDate(LocalDate.of(2026, 9, 1));
        expense.setDescription("Old description");
        expense.setExpAmount(new BigDecimal("1000"));
        expense.setPaymentMethod("Cash");

        when(expenseRepository.findByExpIdAndUserIdEmail(1L, "test@example.com")).thenReturn(Optional.of(expense));

        service.updateExpense(1L, LocalDate.of(2026, 9, 4), "New description", new BigDecimal("2000"),  "Credit Card", "test@example.com");

        assertEquals(LocalDate.of(2026, 9, 4), expense.getExpDate());
        assertEquals("New description", expense.getDescription());
        assertEquals(new BigDecimal("2000"), expense.getExpAmount());
        assertEquals("Credit Card", expense.getPaymentMethod());

        verify(expenseRepository).save(expense);
    }

    @Test
    void deleteExpenseTest(){
        service = new ExpenseService(expenseRepository, userRepository);
        ExpenseEntity expense = new ExpenseEntity();

        when(expenseRepository.findByExpIdAndUserIdEmail(1L, "test@example.com")).thenReturn(Optional.of(expense));
        service.deleteExpense(1L, "test@example.com");        
        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpenseTest_notFound(){
        service = new ExpenseService(expenseRepository, userRepository);
        when(expenseRepository.findByExpIdAndUserIdEmail(1L, "test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,()-> service.deleteExpense(1L, "test@example.com"));
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void addExpenseTest(){
        service = new ExpenseService(expenseRepository, userRepository);
        UserEntity user = new UserEntity();
        ExpenseEntity expense = new ExpenseEntity();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        service.addExpense(expense, "test@example.com");
        assertEquals(user, expense.getUserId());
        verify(expenseRepository).save(expense);

    }

    @Test
    void addExpenseTest_userNotFound(){
        service = new ExpenseService(expenseRepository, userRepository);
        ExpenseEntity expense = new ExpenseEntity();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.addExpense(expense, "test@example.com"));
        
        verify(expenseRepository, never()).save(any());
    }

}
