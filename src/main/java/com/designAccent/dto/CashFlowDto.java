package com.designAccent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashFlowDto {

    private BigDecimal totalInflow;
    private BigDecimal totalOutflow;
    private BigDecimal vendorDues;
    private BigDecimal netPosition;
    private String month;

    public BigDecimal getTotalInflow() { return totalInflow; }
    public void setTotalInflow(BigDecimal totalInflow) { this.totalInflow = totalInflow; }

    public BigDecimal getTotalOutflow() { return totalOutflow; }
    public void setTotalOutflow(BigDecimal totalOutflow) { this.totalOutflow = totalOutflow; }

    public BigDecimal getVendorDues() { return vendorDues; }
    public void setVendorDues(BigDecimal vendorDues) { this.vendorDues = vendorDues; }

    public BigDecimal getNetPosition() { return netPosition; }
    public void setNetPosition(BigDecimal netPosition) { this.netPosition = netPosition; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
}