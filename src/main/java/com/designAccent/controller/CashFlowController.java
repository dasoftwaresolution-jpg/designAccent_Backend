package com.designAccent.controller;

import com.designAccent.dto.ApiResponse;
import com.designAccent.dto.CashFlowDto;
import com.designAccent.service.CashFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cashflow")
@CrossOrigin
public class CashFlowController {

    @Autowired
    private CashFlowService cashFlowService;

    // GET ALL TIME CASH FLOW
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<CashFlowDto>> getAllCashFlow() {
        CashFlowDto data = cashFlowService.getCashFlow();
        return ResponseEntity.ok(ApiResponse.success("Cash flow data", data));
    }

    // GET CASH FLOW BY MONTH
    @GetMapping("/month")
    public ResponseEntity<ApiResponse<CashFlowDto>> getCashFlowByMonth(
            @RequestParam int month,
            @RequestParam int year) {
        CashFlowDto data = cashFlowService.getCashFlowByMonth(month, year);
        return ResponseEntity.ok(ApiResponse.success("Cash flow data", data));
    }
}
