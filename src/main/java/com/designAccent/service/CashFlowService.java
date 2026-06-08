package com.designAccent.service;

import com.designAccent.dto.CashFlowDto;
import com.designAccent.repository.ExpenseRepository;
import com.designAccent.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CashFlowService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    // GET CASH FLOW - ALL TIME
    public CashFlowDto getCashFlow() {

        // total inflow = sum of all project total_amount
        BigDecimal totalInflow = projectRepository.sumTotalAmount();

        // total outflow = sum of all expenses amount
        BigDecimal totalOutflow = expenseRepository.sumAllAmount();

        // vendor dues = sum of (total_budget - total_amount) per project
        BigDecimal vendorDues = projectRepository.sumVendorDues();

        // net position = total inflow - total outflow
        BigDecimal netPosition = totalInflow.subtract(totalOutflow);

        CashFlowDto dto = new CashFlowDto();
        dto.setTotalInflow(totalInflow);
        dto.setTotalOutflow(totalOutflow);
        dto.setVendorDues(vendorDues);
        dto.setNetPosition(netPosition);
        dto.setMonth("All Projects");
        return dto;
    }

    // GET CASH FLOW - BY MONTH
    public CashFlowDto getCashFlowByMonth(int month, int year) {

        // total inflow for this month
        BigDecimal totalInflow = projectRepository.sumTotalAmountByMonth(month, year);

        // total outflow for this month
        BigDecimal totalOutflow = expenseRepository.sumAmountByMonth(month, year);

        // vendor dues for this month
        BigDecimal vendorDues = projectRepository.sumVendorDuesByMonth(month, year);

        // net position = total inflow - total outflow
        BigDecimal netPosition = totalInflow.subtract(totalOutflow);

        // format month name
        LocalDateTime date = LocalDateTime.of(year, month, 1, 0, 0);
        String monthName = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        CashFlowDto dto = new CashFlowDto();
        dto.setTotalInflow(totalInflow);
        dto.setTotalOutflow(totalOutflow);
        dto.setVendorDues(vendorDues);
        dto.setNetPosition(netPosition);
        dto.setMonth(monthName);
        return dto;
    }
}