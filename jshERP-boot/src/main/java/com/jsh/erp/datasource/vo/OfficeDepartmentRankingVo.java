package com.jsh.erp.datasource.vo;

import java.math.BigDecimal;

public class OfficeDepartmentRankingVo {

    private String departmentName;

    private BigDecimal quantity;

    private BigDecimal amount;

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
