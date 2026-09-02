package com.ivy.expense_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDto {

    private LocalDate date;
    private String description;
    private String type;
    private BigDecimal amount;

    //Constructor
    public TransactionDto(LocalDate date,
                          String description,
                          String type,
                          BigDecimal amount
    ){
        this.date = date;
        this.description = description;
        this.type = type;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }



}
