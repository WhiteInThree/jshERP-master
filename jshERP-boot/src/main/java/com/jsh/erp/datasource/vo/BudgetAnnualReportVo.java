package com.jsh.erp.datasource.vo;

import java.math.BigDecimal;

public class BudgetAnnualReportVo {
    private Long organizationId;
    private String organizationName;
    private BigDecimal initialBudget = BigDecimal.ZERO;
    private final BigDecimal[] carryOvers = new BigDecimal[12];
    private final BigDecimal[] expenses = new BigDecimal[12];
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal yearEndBalance = BigDecimal.ZERO;

    public BudgetAnnualReportVo() {
        for (int i = 0; i < 12; i++) {
            expenses[i] = BigDecimal.ZERO;
        }
    }

    public void setExpense(Integer month, BigDecimal amount) {
        if (month != null && month >= 1 && month <= 12) {
            expenses[month - 1] = amount == null ? BigDecimal.ZERO : amount;
        }
    }

    public void calculateBalances() {
        BigDecimal balance = initialBudget == null ? BigDecimal.ZERO : initialBudget;
        totalExpense = BigDecimal.ZERO;
        for (int i = 0; i < 12; i++) {
            carryOvers[i] = balance;
            totalExpense = totalExpense.add(expenses[i]);
            balance = balance.subtract(expenses[i]);
        }
        yearEndBalance = balance;
    }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public BigDecimal getInitialBudget() { return initialBudget; }
    public void setInitialBudget(BigDecimal initialBudget) { this.initialBudget = initialBudget; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public BigDecimal getYearEndBalance() { return yearEndBalance; }

    public BigDecimal getMonth1CarryOver() { return carryOvers[0]; }
    public BigDecimal getMonth2CarryOver() { return carryOvers[1]; }
    public BigDecimal getMonth3CarryOver() { return carryOvers[2]; }
    public BigDecimal getMonth4CarryOver() { return carryOvers[3]; }
    public BigDecimal getMonth5CarryOver() { return carryOvers[4]; }
    public BigDecimal getMonth6CarryOver() { return carryOvers[5]; }
    public BigDecimal getMonth7CarryOver() { return carryOvers[6]; }
    public BigDecimal getMonth8CarryOver() { return carryOvers[7]; }
    public BigDecimal getMonth9CarryOver() { return carryOvers[8]; }
    public BigDecimal getMonth10CarryOver() { return carryOvers[9]; }
    public BigDecimal getMonth11CarryOver() { return carryOvers[10]; }
    public BigDecimal getMonth12CarryOver() { return carryOvers[11]; }
    public BigDecimal getMonth1Expense() { return expenses[0]; }
    public BigDecimal getMonth2Expense() { return expenses[1]; }
    public BigDecimal getMonth3Expense() { return expenses[2]; }
    public BigDecimal getMonth4Expense() { return expenses[3]; }
    public BigDecimal getMonth5Expense() { return expenses[4]; }
    public BigDecimal getMonth6Expense() { return expenses[5]; }
    public BigDecimal getMonth7Expense() { return expenses[6]; }
    public BigDecimal getMonth8Expense() { return expenses[7]; }
    public BigDecimal getMonth9Expense() { return expenses[8]; }
    public BigDecimal getMonth10Expense() { return expenses[9]; }
    public BigDecimal getMonth11Expense() { return expenses[10]; }
    public BigDecimal getMonth12Expense() { return expenses[11]; }
}
