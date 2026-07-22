package com.jsh.erp.datasource.vo;

import java.math.BigDecimal;

public class OfficeDashboardVo {

    private Long pendingApplyCount;

    private Long pendingIssueCount;

    private Long monthApplyCount;

    private Long monthIssueCount;

    private BigDecimal totalIssueQuantity;

    private BigDecimal totalIssueAmount;

    public Long getPendingApplyCount() {
        return pendingApplyCount;
    }

    public void setPendingApplyCount(Long pendingApplyCount) {
        this.pendingApplyCount = pendingApplyCount;
    }

    public Long getPendingIssueCount() {
        return pendingIssueCount;
    }

    public void setPendingIssueCount(Long pendingIssueCount) {
        this.pendingIssueCount = pendingIssueCount;
    }

    public Long getMonthApplyCount() {
        return monthApplyCount;
    }

    public void setMonthApplyCount(Long monthApplyCount) {
        this.monthApplyCount = monthApplyCount;
    }

    public Long getMonthIssueCount() {
        return monthIssueCount;
    }

    public void setMonthIssueCount(Long monthIssueCount) {
        this.monthIssueCount = monthIssueCount;
    }

    public BigDecimal getTotalIssueQuantity() {
        return totalIssueQuantity;
    }

    public void setTotalIssueQuantity(BigDecimal totalIssueQuantity) {
        this.totalIssueQuantity = totalIssueQuantity;
    }

    public BigDecimal getTotalIssueAmount() {
        return totalIssueAmount;
    }

    public void setTotalIssueAmount(BigDecimal totalIssueAmount) {
        this.totalIssueAmount = totalIssueAmount;
    }
}
