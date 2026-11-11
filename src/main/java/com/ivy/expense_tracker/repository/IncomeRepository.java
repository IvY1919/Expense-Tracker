package com.ivy.expense_tracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ivy.expense_tracker.database.IncomeEntity;
import com.ivy.expense_tracker.database.UserEntity;

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long>{
    List<IncomeEntity> findByUserIdEmail(String email);
    Optional<IncomeEntity> findByIncomeIdAndUserIdEmail(Long incomeId,String email);

    void deleteByUserId(UserEntity userId);

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

    @Query("""
           SELECT
                FUNCTION('DATE_TRUNC', 'month', c.incomeDate),
                SUM(c.incomeAmount)
            FROM IncomeEntity c
            WHERE c.userId.email = :email
            AND c.incomeDate BETWEEN :startDate AND :endDate
            GROUP BY FUNCTION('DATE_TRUNC', 'month', c.incomeDate)
            ORDER BY FUNCTION('DATE_TRUNC', 'month', c.incomeDate) 
            """)
    List<Object[]> getMonthlyTotalsForChart(
        @Param("email") String email,
        @Param("startDate") LocalDate starDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("""
           SELECT
                FUNCTION('DATE_TRUNC', 'year', e.incomeDate),
                SUM(e.incomeAmount)
            FROM IncomeEntity e
            WHERE e.userId.email = :email
            AND e.incomeDate BETWEEN :startDate AND :endDate
            GROUP BY FUNCTION('DATE_TRUNC', 'year', e.incomeDate)
            ORDER BY FUNCTION('DATE_TRUNC', 'year', e.incomeDate) 
            """)
    List<Object[]> getYearlyTotalsForChart(
        @Param("email") String email,
        @Param("startDate") LocalDate starDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT SUM(e.incomeAmount)
            FROM IncomeEntity e
            WHERE e.userId.email = :email
            AND e.incomeDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal getMonthlyIncomeTotal(
        @Param("email") String email,
        @Param("startDate") LocalDate starDate,
        @Param("endDate") LocalDate endDate
    );

    @Query ("""
        SELECT COALESCE(SUM(i.incomeAmount), 0) FROM IncomeEntity i WHERE i.userId.email = :email""")
        BigDecimal getTotalIncome(@Param("email") String email);

    @Query ("""
        SELECT SUM(c.incomeAmount)
        FROM IncomeEntity c
        WHERE c.userId.email = :email
        AND c.incomeDate BETWEEN :startDate AND :endDate
        AND c.type <> 'One-time' 
    """)
    BigDecimal getAverageIncomeTotal(
        @Param("email") String email,
        @Param("startDate") LocalDate starDate,
        @Param("endDate") LocalDate endDate
    );


    
}
