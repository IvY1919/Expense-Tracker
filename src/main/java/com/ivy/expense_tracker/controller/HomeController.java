package com.ivy.expense_tracker.controller;

import com.ivy.expense_tracker.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ivy.expense_tracker.database.ExpenseEntity;
import com.ivy.expense_tracker.database.IncomeEntity;
import com.ivy.expense_tracker.database.UserEntity;
import com.ivy.expense_tracker.service.CustomUserDetails;
import com.ivy.expense_tracker.service.ExpenseService;
import com.ivy.expense_tracker.service.IncomeService;
import com.ivy.expense_tracker.service.ReportService;
import com.ivy.expense_tracker.dto.TransactionDto;
import com.ivy.expense_tracker.repository.UserRepository;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class HomeController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ReportService reportService;

    HomeController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // private final HomeController(ReportService reportService){
    //     this.reportService = reportService;
    // }

    @GetMapping("/dashboard")
    public String dashboard(
        @RequestParam(required = false) String year,
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
        

        if(year == null || year.isBlank()){
            year = Year.now().toString();
        }
        int selectedYear = Integer.parseInt(year);       

        model.addAttribute("selectedYear", selectedYear);
        List<BigDecimal> expenseMonthlyTotals = expenseService.getMonthlyTotals(email, selectedYear);
        model.addAttribute("expenseMonthlyTotals", expenseMonthlyTotals);
        List<BigDecimal> incomeMonthlyTotals = incomeService.getMonthlyTotalsForChart(email, selectedYear);
        model.addAttribute("incomeMonthlyTotals", incomeMonthlyTotals);
        List<TransactionDto> latestTransactions = reportService.getLatestTransactions(email);
        model.addAttribute("latestTransactions", latestTransactions);

        // Get user targetAmount
        BigDecimal targetAmount = userService.getTargetAmount(email);
        BigDecimal monthlySpent = expenseService.getCurrentMonthTotal(email);
        double budgetPercentage  = 0.0;
        if(targetAmount !=null && targetAmount.compareTo(BigDecimal.ZERO) > 0){
            budgetPercentage  = monthlySpent.divide(targetAmount,4,RoundingMode.HALF_UP).doubleValue()*60.0;
        }
        budgetPercentage  = Math.min(budgetPercentage , 100.0);

        BigDecimal totalsaving = reportService.getTotal(email);

        model.addAttribute("monthlySpent", monthlySpent);
        model.addAttribute("targetAmount", targetAmount);
        model.addAttribute("budgetPercentage", budgetPercentage);
        model.addAttribute("totalsaving", totalsaving);

        return "dashboard";
    }

    @GetMapping("/alltransactions")
    public String viewAllTransactions(Model model,
        @RequestParam (defaultValue = "0") int page,
        Authentication authentication) {
        String email = authentication.getName();
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();

        // model.addAttribute("title", "Expense Tracker");
        model.addAttribute("username", userDetails.getUsernameFromDb());
        List<TransactionDto> allTransactions = reportService.getAllTransactions(email);
        // model.addAttribute("allTransactions", allTransactions);

        int pageSize = 10;
        int totalPage = (int) Math.ceil((double) allTransactions.size()/pageSize);
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allTransactions.size());

        List<TransactionDto> transactionsForPage = allTransactions.subList(start, end);

        model.addAttribute("allTransactions", transactionsForPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPage);
        return "alltransactions";
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
            // System.out.println("Controller monthly totals: " + monthlyTotals);
        }          
        return "expense";
    }

    @PostMapping("/expense/updateexpense")
    public String updateExpense(@RequestParam Long expId,
                                @RequestParam LocalDate editExpenseDate,
                                @RequestParam String description,
                                @RequestParam BigDecimal expAmount,
                                @RequestParam String paymentMethod,
                                @RequestParam String month,
                                @RequestParam String view,
                                Authentication authentication) {

    String email = authentication.getName();
    expenseService.updateExpense( expId, editExpenseDate, description, expAmount, paymentMethod, email);
    return "redirect:/expense?month=" + month + "&view=" + view;
    }

    @PostMapping("/expense/deleteexpense")
    public String deleteExpense(
            @RequestParam Long expId,
            @RequestParam String view,
            @RequestParam String month,
            Authentication authentication) {

        String email = authentication.getName();

        expenseService.deleteExpense(expId, email);

        return "redirect:/expense?month=" + month + "&view=" + view;
    }

    @PostMapping("/expense/addexpense")
    public String addExpense(
            @RequestParam String month,
            @RequestParam String view,
            @RequestParam LocalDate addExpenseDate,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal expAmount,
            @RequestParam String paymentMethod,
            Authentication authentication) {

        String email = authentication.getName();

        ExpenseEntity expense = new ExpenseEntity();
        expense.setExpDate(addExpenseDate);
        expense.setDescription(description);
        expense.setExpAmount(expAmount);
        expense.setPaymentMethod(paymentMethod);
        
        expenseService.addExpense(expense, email);

        return "redirect:/expense?month=" + month + "&view=" + view;
    }


    @PostMapping("/expense/addmonthlyexpense")
    public String addMonthlyExpense(@RequestParam String startMonth,
                             @RequestParam String endMonth,
                             @RequestParam Integer expenseDay,
                             @RequestParam String description,
                             @RequestParam BigDecimal expAmount,
                             @RequestParam String paymentMethod,
                             @RequestParam String month,
                             @RequestParam String view,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes ) {

        if (expenseDay == null) {
            redirectAttributes.addFlashAttribute("monthError","Please select an expense day.");
            return "redirect:/expense?month=" + month + "&view=" + view;
        }
        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = YearMonth.parse(endMonth);
        if(end.isBefore(start)){
            redirectAttributes.addFlashAttribute("monthError", "End Month cannot be earlier than Start Month.");
            return "redirect:/expense?month=" + month + "&view=" + view;
        }
        String email = authentication.getName();
        expenseService.addMonthlyexpense(startMonth, endMonth, expenseDay, description, expAmount, paymentMethod, email);
        
        return "redirect:/expense?month=" + month + "&view=" + view;
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
            @RequestParam String month,
            @RequestParam String view,
            @RequestParam LocalDate addIncomeDate,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal incomeAmount,
            Authentication authentication) {

        String email = authentication.getName();

        IncomeEntity income = new IncomeEntity();
        income.setIncomeDate(addIncomeDate);
        income.setDescription(description);
        income.setIncomeAmount(incomeAmount);

        incomeService.addIncome(income, email);

        return "redirect:/income?month=" + month + "&view=" + view;
    }

    @PostMapping("/income/editincome")
    public String editIncome(@RequestParam Long incomeId,
                             @RequestParam LocalDate editIncomeDate,
                             @RequestParam String description,
                             @RequestParam BigDecimal incomeAmount,
                             @RequestParam String month,
                             @RequestParam String view,
                             Authentication authentication ) {
        
        String email = authentication.getName();
        incomeService.updateIncome( incomeId, editIncomeDate, description, incomeAmount, email);
        
        return "redirect:/income?month=" + month + "&view=" + view;
    }

    @PostMapping("/income/addmonthlyincome")
    public String addMonthlyIncome(@RequestParam String startMonth,
                             @RequestParam String endMonth,
                             @RequestParam Integer incomeDay,
                             @RequestParam String description,
                             @RequestParam BigDecimal incomeAmount,
                             @RequestParam String month,
                             @RequestParam String view,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes ) {

        if (incomeDay == null) {
            redirectAttributes.addFlashAttribute("monthError","Please select an income day.");
            return "redirect:/income?month=" + month + "&view=" + view;
        }

        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = YearMonth.parse(endMonth);
        if(end.isBefore(start)){
            redirectAttributes.addFlashAttribute("monthError", "End Month cannot be earlier than Start Month.");
            return "redirect:/income?month=" + month + "&view=" + view;
        }

        String email = authentication.getName();
        incomeService.addMonthlyIncome(startMonth, endMonth, incomeDay, description, incomeAmount, email);
        
        return "redirect:/income?month=" + month + "&view=" + view;
    }
    

    @PostMapping("/income/deleteincome")
    public String deleteIncome(@RequestParam Long incomeId,
                               @RequestParam String month,
                               @RequestParam String view,
                                Authentication authentication ) {
        String email = authentication.getName();
        incomeService.deleteIncome(incomeId, email);
        
        return "redirect:/income?month=" + month + "&view=" + view;
    }

    @PostMapping("/budget/add")
    public String addBudget(@RequestParam("targetAmount") BigDecimal targetAmount,
                            Authentication authentication) {
        
        String email = authentication.getName();
        userService.updateTargetAmount(email, targetAmount);
        
        return "redirect:/dashboard";
    }
    
    @GetMapping("/account_setting")
    public String accountsetting(
        Model model,Authentication authentication) {

        String email = authentication.getName();
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        
        UserEntity user = userService.findByEmail(email);

        model.addAttribute("username", userDetails.getUsernameFromDb());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());

        return "account_setting";
    }

    @PostMapping("/account/delete")
    public String deleteAccount(Authentication authentication) {
       
        String email = authentication.getName();
        userService.deleteAccount(email);        
        return "redirect:/login";
    }
    
    @PostMapping("/account/update-username")
    @ResponseBody
    public ResponseEntity<String> updateUsername(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        String email = authentication.getName();
        String newUsername = request.get("username");
        // Update username in database
        userService.updateUsername(email, newUsername);
        // Get updated user from database
        UserEntity updatedUser = userService.findByEmail(email);
        // Create updated CustomUserDetails
        CustomUserDetails updatedUserDetails = new CustomUserDetails(updatedUser);
        // Create new Authentication
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(updatedUserDetails, authentication.getCredentials(),updatedUserDetails.getAuthorities());
        // Update Spring Security Context
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

        return ResponseEntity.ok("Username updated");

    }

@PostMapping("/account/changepassword")
public String changeAccPwd(
        @RequestParam("currentpassword") String currentPassword,
        @RequestParam("newpassword") String newPassword,
        Authentication authentication,
        RedirectAttributes redirectAttributes,
        Model model) {

    CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();

    String storedPassword = userDetails.getPassword();

    if (!passwordEncoder.matches(currentPassword, storedPassword)) {
        // model.addAttribute("passwordError", "Incorrect password.");
        redirectAttributes.addFlashAttribute("passwordError", "Incorrect password.");
        return "redirect:/account_setting";
    }  

    String encodedPassword = passwordEncoder.encode(newPassword);
    userService.updatePassword(
            userDetails.getUsername(),
            encodedPassword
    );
    redirectAttributes.addFlashAttribute("passwordChanged",true);
    return "redirect:/account_setting";
}
    

    
    
    



    

}
