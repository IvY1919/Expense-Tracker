package com.ivy.expense_tracker.config;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ivy.expense_tracker.repository.UserRepository;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler{

    private final UserRepository userRepository;
        public CustomAuthenticationFailureHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

        @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String email = request.getParameter("email");

        if (userRepository.findByEmail(email).isEmpty()) {
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "/login?emailNotFound=true"
            );
        } else {
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "/login?loginError=true"
            );
        }
    }    
    
    
}
