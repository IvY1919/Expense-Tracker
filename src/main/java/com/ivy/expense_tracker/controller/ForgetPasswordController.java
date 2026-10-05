package com.ivy.expense_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.ivy.expense_tracker.database.PasswordResetTokenEntity;
import com.ivy.expense_tracker.database.UserEntity;
import com.ivy.expense_tracker.dto.ChangePasswordDto;
import com.ivy.expense_tracker.repository.PasswordResetTokenRepository;
import com.ivy.expense_tracker.repository.UserRepository;
import com.ivy.expense_tracker.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
public class ForgetPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ForgetPasswordController(UserRepository userRepository,
                                    PasswordResetTokenRepository passwordResetTokenRepository,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/forgetPwd")
        public String forgetPwd() {
        return "forgetPwd";
    }

    @PostMapping("/forgetPwd")
    public String forgetPwd(@RequestParam String email) {

        String token = UUID.randomUUID().toString();
        String resetLink = "http://localhost:8080/changePwd?token=" + token;

        Optional<UserEntity> user = userRepository.findByEmail(email);

        if (user.isEmpty()) { return "redirect:/forgetPwd?emailNotFound"; }

        UserEntity userEntity = user.get();        
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();   

        resetToken.setToken(token);
        resetToken.setUserId(userEntity);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(userEntity.getEmail(), resetLink);

        System.out.println("Token: " + token);
        System.out.println("Email received: " + email);
        // System.out.println("User found: " + user.isPresent());

        return "redirect:/forgetPwd?emailSent";
    }

        
    @GetMapping("/changePwd")
    public String showChangePasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "changePwd";
    }

    @PostMapping("/changePwd")
    @ResponseBody
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordDto request) {

    System.out.println("===== MY CHANGE PASSWORD CONTROLLER =====");
    System.out.println("Token received: " + request.getToken());
    System.out.println("New password received: " + request.getNewPassword());

        Optional<PasswordResetTokenEntity> resetTokenOptional =
            passwordResetTokenRepository.findByToken(request.getToken());

        if (resetTokenOptional.isEmpty()) {
            System.out.println("Token NOT FOUND");
            return ResponseEntity.badRequest().body("Invalid reset token.");
        }

        PasswordResetTokenEntity resetToken = resetTokenOptional.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            System.out.println("Token EXPIRED");
            return ResponseEntity.badRequest().body("This reset link has expired.");
        }

        UserEntity user = resetToken.getUserId();
        System.out.println("User: " + user.getEmail());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        System.out.println("User saved");
        passwordResetTokenRepository.delete(resetToken);

        System.out.println("Reset token deleted");
        System.out.println("===========================");

        return ResponseEntity.ok("THIS IS MY CONTROLLER");
        
    }
    
    


}
