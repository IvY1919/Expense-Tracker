package com.ivy.expense_tracker.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service 
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink){

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("Expense Tracker <amelia.torres01011990@gmail.com>");
        message.setTo(toEmail);
        message.setSubject("Password Reset");
        message.setText(
            "Please click the following link to reset your password:\n\n"
            + resetLink
            + "\n\n"
            + "This link is valid for 30 minutes."
        );

        mailSender.send(message);
    }

}
