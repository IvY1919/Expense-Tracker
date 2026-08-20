package com.ivy.expense_tracker.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ivy.expense_tracker.database.UserEntity;

public interface UserRepository extends JpaRepository <UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);    
}
