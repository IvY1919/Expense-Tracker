package com.ivy.expense_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ivy.expense_tracker.database.PasswordResetTokenEntity;
import com.ivy.expense_tracker.database.UserEntity;

import java.util.Optional;


public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByToken(String token);
    void deleteByUserId(UserEntity userId);

    // void deleteByUser(User user);
}
