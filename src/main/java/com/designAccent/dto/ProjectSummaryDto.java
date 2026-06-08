package com.designAccent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectSummaryDto {

    // top header cards
    private BigDecimal totalValue;      // sum of total_budget current month
    private BigDecimal totalReceived;   // sum of total_amount current month
    private BigDecimal totalDues;       // totalValue - totalReceived current month

    // status counts
    private Long activeCount;
    private Long holdCount;
    private Long doneCount;

    // project list
    private List<ProjectDto> projects;

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getTotalReceived() { return totalReceived; }
    public void setTotalReceived(BigDecimal totalReceived) { this.totalReceived = totalReceived; }

    public BigDecimal getTotalDues() { return totalDues; }
    public void setTotalDues(BigDecimal totalDues) { this.totalDues = totalDues; }

    public Long getActiveCount() { return activeCount; }
    public void setActiveCount(Long activeCount) { this.activeCount = activeCount; }

    public Long getHoldCount() { return holdCount; }
    public void setHoldCount(Long holdCount) { this.holdCount = holdCount; }

    public Long getDoneCount() { return doneCount; }
    public void setDoneCount(Long doneCount) { this.doneCount = doneCount; }

    public List<ProjectDto> getProjects() { return projects; }
    public void setProjects(List<ProjectDto> projects) { this.projects = projects; }
}