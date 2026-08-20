package com.ivy.expense_tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.ivy.expense_tracker.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {
    
    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }    

    @PostMapping("/signup")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password) {
        userService.registerUser(username, email, password);        
        return "redirect:/login";
    }  


    
}
