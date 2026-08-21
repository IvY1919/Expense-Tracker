package com.ivy.expense_tracker.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

import com.ivy.expense_tracker.database.ExpenseEntity;
import com.ivy.expense_tracker.service.CustomUserDetails;
import com.ivy.expense_tracker.service.ExpenseService;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class HomeController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/dashboard")
    public String dashboard(
        Model model,
        Authentication authentication){

        String email = authentication.getName();
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();

        model.addAttribute("title", "Expense Tracker");
        model.addAttribute("username", userDetails.getUsernameFromDb());

        BigDecimal monthlyTotal = expenseService.getCurrentMonthTotal(email);
        model.addAttribute("monthlyTotal", monthlyTotal);
        return "dashboard";
    }

    @GetMapping("/expense")
    public String expense(
        @RequestParam(defaultValue = "monthly") String view,
        @RequestParam(required = false) String month,
        Model model,
        Authentication authentication) {

        String email = authentication.getName();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        model.addAttribute("username", userDetails.getUsernameFromDb());

        int currentYear = LocalDate.now().getYear();
        model.addAttribute("view", view);
        model.addAttribute("year", currentYear);

    // Default to current month
        if (month == null || month.isBlank()) {
            month = YearMonth.now().toString();
        }

        YearMonth selectedYearMonth = YearMonth.parse(month);

        LocalDate startDate = selectedYearMonth.atDay(1);
        LocalDate endDate = selectedYearMonth.atEndOfMonth();

        List<ExpenseEntity> expenses = expenseService.getExpensesByUserIdAndDate( email, startDate, endDate);

        model.addAttribute("expenses", expenses);
        model.addAttribute("selectedMonth", selectedYearMonth);

     // Get total for selected month
        BigDecimal monthlyTotal =
                expenseService.getMonthlyTotal( email, startDate, endDate);

        model.addAttribute("monthlyTotal", monthlyTotal);

    // for donutchart
        Map<String, BigDecimal> categoryTotals = expenseService.getCategoryTotals(email, startDate, endDate);
        model.addAttribute("categoryTotals", categoryTotals);

    // for linechart
        
        if ("yearly".equals(view)) {            
            List<BigDecimal> yearlyTotals = expenseService.getYearlyTotals(email, currentYear);
            model.addAttribute("yearlyTotals", yearlyTotals);

        } else {
            List<BigDecimal> monthlyTotals = expenseService.getMonthlyTotals(email, currentYear);
            model.addAttribute("monthlyTotals", monthlyTotals);
            System.out.println("Controller monthly totals: " + monthlyTotals);
        }          
        return "expense";
    }

    @PostMapping("/expense/updateexpense")
    public String updateExpense(@RequestParam Long expId,
                                @RequestParam LocalDate expDate,
                                @RequestParam String description,
                                @RequestParam BigDecimal expAmount,
                                @RequestParam String paymentMethod,
                                Authentication authentication) {

    String email = authentication.getName();
    expenseService.updateExpense( expId, expDate, description, expAmount, paymentMethod, email);
    return "redirect:/expense";
    }

    @PostMapping("/expense/deleteexpense")
    public String deleteExpense(
            @RequestParam Long expId,
            Authentication authentication) {

        String email = authentication.getName();

        expenseService.deleteExpense(expId, email);

        return "redirect:/expense";
    }

    @PostMapping("/expense/addexpense")
    public String AddExpense(
            @ModelAttribute ExpenseEntity expense,
            Authentication authentication) {

        String email = authentication.getName();

        expenseService.addExpense(expense, email);

        return "redirect:/expense";
    }


    

}
