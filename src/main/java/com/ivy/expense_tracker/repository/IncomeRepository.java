package com.ivy.expense_tracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ivy.expense_tracker.database.IncomeEntity;

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long>{
    List<IncomeEntity> findByUserIdEmail(String email);
    Optional<IncomeEntity> findByIncomeIdAndUserIdEmail(Long incomeId,String email);

    @Query("""
        SELECT c
        FROM IncomeEntity c
        WHERE c.userId.email = :email
        AND c.incomeDate BETWEEN :startDate AND :endDate
        ORDER BY c.incomeDate DESC
        """)
    List<IncomeEntity> getIncomeByUserIdANDDate(
        @Param("email") String email,
        @Param("startDate") LocalDate starDate,
        @Param("endDate") LocalDate enDate
    );
    
    // Get total expense for current month
    @Query("""
            SELECT SUM(c.incomeAmount)
            FROM IncomeEntity c
            WHERE c.userId.email = :email
            AND c.incomeDate BETWEEN :startDate AND :endDate
            """)

    BigDecimal getIncomeMonthlyTotal(
        @Param("email") String email,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
        );

    
}
