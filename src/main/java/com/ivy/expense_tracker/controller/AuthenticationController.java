package com.ivy.expense_tracker.controller;

import com.ivy.expense_tracker.service.UserService;
import com.ivy.expense_tracker.repository.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class AuthenticationController {
    
    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private UserRepository userRepository;

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

    // @PostMapping("/login")
    // public String postMethodName(@RequestParam String email, @RequestParam String password, Model model) {
    //     Optional<UserEntity> user = userRepository.findByEmail(email);
    //     if (user.isEmpty()) {
    //     model.addAttribute("emailNotFound", "Email not found");
    //     return "login";
    //     }
        
    //     return "redirect:/dashboard";
    // }
    


    
}
