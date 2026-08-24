package com.ivy.expense_tracker.service;
import com.ivy.expense_tracker.database.UserEntity;
import com.ivy.expense_tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
// import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// Finds the user 
@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException("Email not found"));

        return new CustomUserDetails(user);
    }
}
