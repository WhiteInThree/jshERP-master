package com.jsh.erp.datasource.vo;

import java.math.BigDecimal;

public class BudgetMonthlyExpenseVo {
    private Long organizationId;
    private Integer expenseMonth;
    private BigDecimal expenseAmount;

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public Integer getExpenseMonth() { return expenseMonth; }
    public void setExpenseMonth(Integer expenseMonth) { this.expenseMonth = expenseMonth; }
    public BigDecimal getExpenseAmount() { return expenseAmount; }
    public void setExpenseAmount(BigDecimal expenseAmount) { this.expenseAmount = expenseAmount; }
}
