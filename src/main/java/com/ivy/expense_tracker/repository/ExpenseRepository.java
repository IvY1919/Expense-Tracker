package com.ivy.expense_tracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ivy.expense_tracker.database.ExpenseEntity;
import com.ivy.expense_tracker.database.UserEntity;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long>{

    List<ExpenseEntity> findByUserIdEmail(String email);

    Optional<ExpenseEntity> findByExpIdAndUserIdEmail(Long expId,String email);

    void deleteByUserId(UserEntity userId);

    // Get total expense for current month
    @Query("""
            SELECT SUM(e.expAmount)
            FROM ExpenseEntity e
            WHERE e.userId.email = :email
            AND e.expDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal getMonthlyTotal(
        @Param("email") String email,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
        );

    @Query("""
            SELECT e
            FROM ExpenseEntity e
            WHERE e.userId.email = :email
            AND e.expDate BETWEEN :startDate AND :endDate
            ORDER BY e.expDate DESC
        """)
    List<ExpenseEntity> getExpensesByUserIdAndDate(
            @Param("email") String email,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Get expense total for each month
    @Query("""
           SELECT
                FUNCTION('DATE_TRUNC', 'month', e.expDate),
                SUM(e.expAmount)
            FROM ExpenseEntity e
            WHERE e.userId.email = :email
            AND e.expDate BETWEEN :startDate AND :endDate
            GROUP BY FUNCTION('DATE_TRUNC', 'month', e.expDate)
            ORDER BY FUNCTION('DATE_TRUNC', 'month', e.expDate) 
            """)
    List<Object[]> getMonthlyTotals(
            @Param("email") String email,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Get expense total for each year
    @Query("""
        SELECT
            FUNCTION('DATE_TRUNC', 'year', e.expDate),
            SUM(e.expAmount)
        FROM ExpenseEntity e
        WHERE e.userId.email = :email
        AND e.expDate BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('DATE_TRUNC', 'year', e.expDate)
        ORDER BY FUNCTION('DATE_TRUNC', 'year', e.expDate)
        """)
        List<Object[]> getYearlyTotals(
        @Param("email") String email,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query ("""
        SELECT COALESCE(SUM(e.expAmount), 0) FROM ExpenseEntity e WHERE e.userId.email = :email""")
        BigDecimal getTotalExpense(@Param("email") String email);   
        
}
