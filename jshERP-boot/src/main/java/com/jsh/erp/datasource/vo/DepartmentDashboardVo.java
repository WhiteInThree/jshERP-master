package com.jsh.erp.datasource.vo;

import java.math.BigDecimal;

public class DepartmentDashboardVo {

    private String departmentName;

    private Long pendingApplyCount;

    private Long approvedPendingIssueCount;

    private BigDecimal monthReceivedCount;

    private Long currentStockMaterialCount;

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getPendingApplyCount() {
        return pendingApplyCount;
    }

    public void setPendingApplyCount(Long pendingApplyCount) {
        this.pendingApplyCount = pendingApplyCount;
    }

    public Long getApprovedPendingIssueCount() {
        return approvedPendingIssueCount;
    }

    public void setApprovedPendingIssueCount(Long approvedPendingIssueCount) {
        this.approvedPendingIssueCount = approvedPendingIssueCount;
    }

    public BigDecimal getMonthReceivedCount() {
        return monthReceivedCount;
    }

    public void setMonthReceivedCount(BigDecimal monthReceivedCount) {
        this.monthReceivedCount = monthReceivedCount;
    }

    public Long getCurrentStockMaterialCount() {
        return currentStockMaterialCount;
    }

    public void setCurrentStockMaterialCount(Long currentStockMaterialCount) {
        this.currentStockMaterialCount = currentStockMaterialCount;
    }
}
