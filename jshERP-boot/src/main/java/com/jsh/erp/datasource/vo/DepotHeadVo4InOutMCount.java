package com.jsh.erp.datasource.vo;

import java.math.BigDecimal;

public class DepotHeadVo4InOutMCount {

    private Long materialId;

    private String barCode;

    private String mName;

    private String model;

    private String standard;

    private String color;

    private String brand;

    private String mfrs;

    private String categoryName;

    private String materialUnit;

    private String issueDepartment;

    private BigDecimal numSum;

    private BigDecimal priceSum;

    private BigDecimal branchNumSum;

    private BigDecimal branchPriceSum;

    private BigDecimal officeNumSum;

    private BigDecimal officePriceSum;

    private BigDecimal summaryNumSum;

    private BigDecimal summaryPriceSum;

    private Long tenantId;

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getMfrs() {
        return mfrs;
    }

    public void setMfrs(String mfrs) {
        this.mfrs = mfrs;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getMaterialUnit() {
        return materialUnit;
    }

    public void setMaterialUnit(String materialUnit) {
        this.materialUnit = materialUnit;
    }

    public String getIssueDepartment() {
        return issueDepartment;
    }

    public void setIssueDepartment(String issueDepartment) {
        this.issueDepartment = issueDepartment;
    }

    public BigDecimal getNumSum() {
        return numSum;
    }

    public void setNumSum(BigDecimal numSum) {
        this.numSum = numSum;
    }

    public BigDecimal getPriceSum() {
        return priceSum;
    }

    public void setPriceSum(BigDecimal priceSum) {
        this.priceSum = priceSum;
    }

    public BigDecimal getBranchNumSum() { return branchNumSum; }
    public void setBranchNumSum(BigDecimal branchNumSum) { this.branchNumSum = branchNumSum; }
    public BigDecimal getBranchPriceSum() { return branchPriceSum; }
    public void setBranchPriceSum(BigDecimal branchPriceSum) { this.branchPriceSum = branchPriceSum; }
    public BigDecimal getOfficeNumSum() { return officeNumSum; }
    public void setOfficeNumSum(BigDecimal officeNumSum) { this.officeNumSum = officeNumSum; }
    public BigDecimal getOfficePriceSum() { return officePriceSum; }
    public void setOfficePriceSum(BigDecimal officePriceSum) { this.officePriceSum = officePriceSum; }
    public BigDecimal getSummaryNumSum() { return summaryNumSum; }
    public void setSummaryNumSum(BigDecimal summaryNumSum) { this.summaryNumSum = summaryNumSum; }
    public BigDecimal getSummaryPriceSum() { return summaryPriceSum; }
    public void setSummaryPriceSum(BigDecimal summaryPriceSum) { this.summaryPriceSum = summaryPriceSum; }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
