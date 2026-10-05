package com.ivy.expense_tracker.repository;

import java.lang.foreign.Linker.Option;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ivy.expense_tracker.database.PasswordResetTokenEntity;

import java.util.Optional;


public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    // void deleteByUser(User user);
}
