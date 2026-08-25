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
import com.ivy.expense_tracker.database.IncomeEntity;
import com.ivy.expense_tracker.service.CustomUserDetails;
import com.ivy.expense_tracker.service.ExpenseService;
import com.ivy.expense_tracker.service.IncomeService;
import com.ivy.expense_tracker.service.ReportService;

import org.springframework.web.bind.annotation.PostMapping;



@Controller
public class HomeController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/dashboard")
    public String dashboard(
        Model model,
        Authentication authentication){

        String email = authentication.getName();
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();

        model.addAttribute("title", "Expense Tracker");
        model.addAttribute("username", userDetails.getUsernameFromDb());

        BigDecimal monthlyexpenseTotal = expenseService.getCurrentMonthTotal(email);
        model.addAttribute("monthlyexpenseTotal", monthlyexpenseTotal);
        BigDecimal monthlyincomeTotal = incomeService.getCurrentMonthTotal(email);
        model.addAttribute("monthlyincomeTotal", monthlyincomeTotal);
        BigDecimal monthlyRemainingTotal = reportService.getCurrentMontRemainingTotal(email);
        model.addAttribute("monthlyRemainingTotal", monthlyRemainingTotal);
        
        return "dashboard";
    }

    // For Expense page
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
        int selectedYear = selectedYearMonth.getYear();

        LocalDate startDate = selectedYearMonth.atDay(1);
        LocalDate endDate = selectedYearMonth.atEndOfMonth();

        List<ExpenseEntity> expenses = expenseService.getExpensesByUserIdAndDate( email, startDate, endDate);

        model.addAttribute("expenses", expenses);
        model.addAttribute("selectedMonth", selectedYearMonth);
        model.addAttribute("selectedYear", selectedYear);

     // Get total for selected month
        BigDecimal monthlyTotal =
                expenseService.getMonthlyTotal( email, startDate, endDate);

        model.addAttribute("monthlyTotal", monthlyTotal);

    // for donutchart
        Map<String, BigDecimal> categoryTotals = expenseService.getCategoryTotals(email, startDate, endDate);
        model.addAttribute("categoryTotals", categoryTotals);

    // for linechart
        
        if ("yearly".equals(view)) {            
            List<BigDecimal> yearlyTotals = expenseService.getYearlyTotals(email, selectedYear);
            model.addAttribute("yearlyTotals", yearlyTotals);

        } else {
            List<BigDecimal> monthlyTotals = expenseService.getMonthlyTotals(email, selectedYear);
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
    // End of Expense

    // Income page 
    @GetMapping("/income")
    public String income(
        @RequestParam(required = false) String month,
        @RequestParam(defaultValue = "monthly") String view,
        Model model,
        Authentication authentication){

        String email = authentication.getName();
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("username", userDetails.getUsernameFromDb());

        int currentYear = LocalDate.now().getYear();
        model.addAttribute("year", currentYear);
        model.addAttribute("view", view);

        if (month == null || month.isBlank()) {
            month = YearMonth.now().toString();
        }

        YearMonth selectedYearMonth = YearMonth.parse(month);
        int selectedYear = selectedYearMonth.getYear();

        LocalDate startDate = selectedYearMonth.atDay(1);
        LocalDate endDate = selectedYearMonth.atEndOfMonth();
        List<IncomeEntity> incomes = incomeService.getIncomeByUserIdANDDate( email, startDate, endDate);

        model.addAttribute("incomes", incomes);
        model.addAttribute("selectedMonth", selectedYearMonth);
        model.addAttribute("selectedYear", selectedYear);


        if("yearly".equals(view)){
            List<BigDecimal> yearlyTotals = incomeService.getYearlyTotalsForChart(email, selectedYear);
            model.addAttribute("yearlyTotals",yearlyTotals);

        }else{
            List<BigDecimal> monthlyTotals = incomeService.getMonthlyTotalsForChart(email, selectedYear);
            model.addAttribute("monthlyTotals",monthlyTotals);
        }

        // Get total for selected month
        BigDecimal monthlyincomeTotal = incomeService.getMonthlyIncomeTotal( email, startDate, endDate);
        model.addAttribute("monthlyincomeTotal", monthlyincomeTotal);


        return "income";
    }

    @PostMapping("/income/addincome")
    public String AddIncome(
            @ModelAttribute IncomeEntity income,
            Authentication authentication) {

        String email = authentication.getName();

        incomeService.addIncome(income, email);

        return "redirect:/income";
    }

    @PostMapping("/income/editincome")
    public String editIncome(@RequestParam Long incomeId,
                             @RequestParam LocalDate incomeDate,
                             @RequestParam String description,
                             @RequestParam BigDecimal incomeAmount,
                             Authentication authentication ) {
        
        String email = authentication.getName();
        incomeService.updateIncome( incomeId, incomeDate, description, incomeAmount, email);
        
        return "redirect:/income";
    }

    @PostMapping("/income/deleteincome")
    public String deleteIncome(@RequestParam Long incomeId,
                                Authentication authentication ) {
        String email = authentication.getName();
        incomeService.deleteIncome(incomeId, email);
        
        return "redirect:/income";
    }
    
    



    

}
