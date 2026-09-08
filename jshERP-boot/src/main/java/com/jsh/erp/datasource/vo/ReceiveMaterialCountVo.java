package com.jsh.erp.datasource.vo;

import java.math.BigDecimal;

/**
 * Annual issue quantity summary for one material.
 */
public class ReceiveMaterialCountVo {
    private Long materialId;
    private String mName;
    private String materialUnit;
    private BigDecimal initialInbound = BigDecimal.ZERO;
    private BigDecimal initialIssue = BigDecimal.ZERO;
    private final BigDecimal[] inboundQuantities = new BigDecimal[12];
    private final BigDecimal[] issueQuantities = new BigDecimal[12];

    public ReceiveMaterialCountVo() {
        for (int i = 0; i < 12; i++) {
            inboundQuantities[i] = BigDecimal.ZERO;
            issueQuantities[i] = BigDecimal.ZERO;
        }
    }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getmName() { return mName; }
    public void setmName(String mName) { this.mName = mName; }
    public String getMaterialUnit() { return materialUnit; }
    public void setMaterialUnit(String materialUnit) { this.materialUnit = materialUnit; }

    public BigDecimal getInitialInbound() { return initialInbound; }
    public void setInitialInbound(BigDecimal value) { initialInbound = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getInitialIssue() { return initialIssue; }
    public void setInitialIssue(BigDecimal value) { initialIssue = value == null ? BigDecimal.ZERO : value; }
    private BigDecimal value(BigDecimal[] values, int month) { return values[month - 1] == null ? BigDecimal.ZERO : values[month - 1]; }
    private void set(BigDecimal[] values, int month, BigDecimal value) { values[month - 1] = value == null ? BigDecimal.ZERO : value; }
    private BigDecimal inbound(int month) { return value(inboundQuantities, month); }
    private BigDecimal issue(int month) { return value(issueQuantities, month); }
    private BigDecimal opening(int month) {
        BigDecimal balance = initialInbound.subtract(initialIssue);
        for (int i = 1; i < month; i++) balance = balance.add(inbound(i)).subtract(issue(i));
        return balance;
    }
    private BigDecimal ending(int month) { return opening(month).add(inbound(month)).subtract(issue(month)); }
    public void setMonthInbound(int month, BigDecimal value) { set(inboundQuantities, month, value); }
    public void setMonthIssue(int month, BigDecimal value) { set(issueQuantities, month, value); }
    public void setMonth1Inbound(BigDecimal v) { setMonthInbound(1, v); } public void setMonth1Issue(BigDecimal v) { setMonthIssue(1, v); }
    public void setMonth2Inbound(BigDecimal v) { setMonthInbound(2, v); } public void setMonth2Issue(BigDecimal v) { setMonthIssue(2, v); }
    public void setMonth3Inbound(BigDecimal v) { setMonthInbound(3, v); } public void setMonth3Issue(BigDecimal v) { setMonthIssue(3, v); }
    public void setMonth4Inbound(BigDecimal v) { setMonthInbound(4, v); } public void setMonth4Issue(BigDecimal v) { setMonthIssue(4, v); }
    public void setMonth5Inbound(BigDecimal v) { setMonthInbound(5, v); } public void setMonth5Issue(BigDecimal v) { setMonthIssue(5, v); }
    public void setMonth6Inbound(BigDecimal v) { setMonthInbound(6, v); } public void setMonth6Issue(BigDecimal v) { setMonthIssue(6, v); }
    public void setMonth7Inbound(BigDecimal v) { setMonthInbound(7, v); } public void setMonth7Issue(BigDecimal v) { setMonthIssue(7, v); }
    public void setMonth8Inbound(BigDecimal v) { setMonthInbound(8, v); } public void setMonth8Issue(BigDecimal v) { setMonthIssue(8, v); }
    public void setMonth9Inbound(BigDecimal v) { setMonthInbound(9, v); } public void setMonth9Issue(BigDecimal v) { setMonthIssue(9, v); }
    public void setMonth10Inbound(BigDecimal v) { setMonthInbound(10, v); } public void setMonth10Issue(BigDecimal v) { setMonthIssue(10, v); }
    public void setMonth11Inbound(BigDecimal v) { setMonthInbound(11, v); } public void setMonth11Issue(BigDecimal v) { setMonthIssue(11, v); }
    public void setMonth12Inbound(BigDecimal v) { setMonthInbound(12, v); } public void setMonth12Issue(BigDecimal v) { setMonthIssue(12, v); }
    public BigDecimal getMonth1Opening() { return opening(1); } public BigDecimal getMonth1Inbound() { return inbound(1); } public BigDecimal getMonth1Issue() { return issue(1); } public BigDecimal getMonth1Ending() { return ending(1); }
    public BigDecimal getMonth2Opening() { return opening(2); } public BigDecimal getMonth2Inbound() { return inbound(2); } public BigDecimal getMonth2Issue() { return issue(2); } public BigDecimal getMonth2Ending() { return ending(2); }
    public BigDecimal getMonth3Opening() { return opening(3); } public BigDecimal getMonth3Inbound() { return inbound(3); } public BigDecimal getMonth3Issue() { return issue(3); } public BigDecimal getMonth3Ending() { return ending(3); }
    public BigDecimal getMonth4Opening() { return opening(4); } public BigDecimal getMonth4Inbound() { return inbound(4); } public BigDecimal getMonth4Issue() { return issue(4); } public BigDecimal getMonth4Ending() { return ending(4); }
    public BigDecimal getMonth5Opening() { return opening(5); } public BigDecimal getMonth5Inbound() { return inbound(5); } public BigDecimal getMonth5Issue() { return issue(5); } public BigDecimal getMonth5Ending() { return ending(5); }
    public BigDecimal getMonth6Opening() { return opening(6); } public BigDecimal getMonth6Inbound() { return inbound(6); } public BigDecimal getMonth6Issue() { return issue(6); } public BigDecimal getMonth6Ending() { return ending(6); }
    public BigDecimal getMonth7Opening() { return opening(7); } public BigDecimal getMonth7Inbound() { return inbound(7); } public BigDecimal getMonth7Issue() { return issue(7); } public BigDecimal getMonth7Ending() { return ending(7); }
    public BigDecimal getMonth8Opening() { return opening(8); } public BigDecimal getMonth8Inbound() { return inbound(8); } public BigDecimal getMonth8Issue() { return issue(8); } public BigDecimal getMonth8Ending() { return ending(8); }
    public BigDecimal getMonth9Opening() { return opening(9); } public BigDecimal getMonth9Inbound() { return inbound(9); } public BigDecimal getMonth9Issue() { return issue(9); } public BigDecimal getMonth9Ending() { return ending(9); }
    public BigDecimal getMonth10Opening() { return opening(10); } public BigDecimal getMonth10Inbound() { return inbound(10); } public BigDecimal getMonth10Issue() { return issue(10); } public BigDecimal getMonth10Ending() { return ending(10); }
    public BigDecimal getMonth11Opening() { return opening(11); } public BigDecimal getMonth11Inbound() { return inbound(11); } public BigDecimal getMonth11Issue() { return issue(11); } public BigDecimal getMonth11Ending() { return ending(11); }
    public BigDecimal getMonth12Opening() { return opening(12); } public BigDecimal getMonth12Inbound() { return inbound(12); } public BigDecimal getMonth12Issue() { return issue(12); } public BigDecimal getMonth12Ending() { return ending(12); }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 1; i <= 12; i++) total = total.add(issue(i));
        return total;
    }
}
