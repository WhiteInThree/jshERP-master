package com.jsh.erp.datasource.vo;

import java.math.BigDecimal;

/**
 * Annual issue quantity summary for one material.
 */
public class ReceiveMaterialCountVo {
    private Long materialId;
    private String mName;
    private String materialUnit;
    private final BigDecimal[] monthQuantities = new BigDecimal[12];

    public ReceiveMaterialCountVo() {
        for (int i = 0; i < monthQuantities.length; i++) {
            monthQuantities[i] = BigDecimal.ZERO;
        }
    }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getmName() { return mName; }
    public void setmName(String mName) { this.mName = mName; }
    public String getMaterialUnit() { return materialUnit; }
    public void setMaterialUnit(String materialUnit) { this.materialUnit = materialUnit; }

    private BigDecimal getMonth(int month) { return monthQuantities[month - 1]; }
    private void setMonth(int month, BigDecimal value) { monthQuantities[month - 1] = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getMonth1() { return getMonth(1); }
    public void setMonth1(BigDecimal value) { setMonth(1, value); }
    public BigDecimal getMonth2() { return getMonth(2); }
    public void setMonth2(BigDecimal value) { setMonth(2, value); }
    public BigDecimal getMonth3() { return getMonth(3); }
    public void setMonth3(BigDecimal value) { setMonth(3, value); }
    public BigDecimal getMonth4() { return getMonth(4); }
    public void setMonth4(BigDecimal value) { setMonth(4, value); }
    public BigDecimal getMonth5() { return getMonth(5); }
    public void setMonth5(BigDecimal value) { setMonth(5, value); }
    public BigDecimal getMonth6() { return getMonth(6); }
    public void setMonth6(BigDecimal value) { setMonth(6, value); }
    public BigDecimal getMonth7() { return getMonth(7); }
    public void setMonth7(BigDecimal value) { setMonth(7, value); }
    public BigDecimal getMonth8() { return getMonth(8); }
    public void setMonth8(BigDecimal value) { setMonth(8, value); }
    public BigDecimal getMonth9() { return getMonth(9); }
    public void setMonth9(BigDecimal value) { setMonth(9, value); }
    public BigDecimal getMonth10() { return getMonth(10); }
    public void setMonth10(BigDecimal value) { setMonth(10, value); }
    public BigDecimal getMonth11() { return getMonth(11); }
    public void setMonth11(BigDecimal value) { setMonth(11, value); }
    public BigDecimal getMonth12() { return getMonth(12); }
    public void setMonth12(BigDecimal value) { setMonth(12, value); }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal quantity : monthQuantities) {
            total = total.add(quantity == null ? BigDecimal.ZERO : quantity);
        }
        return total;
    }
}
