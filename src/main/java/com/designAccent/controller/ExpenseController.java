package com.designAccent.controller;

import com.designAccent.dto.ApiResponse;
import com.designAccent.dto.ExpenseDto;
import com.designAccent.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // GET ALL CATEGORIES + EXPENSES FOR A PROJECT
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getProjectExpenses(
            @PathVariable Long projectId) {
        List<ExpenseDto> data = expenseService.getProjectExpenses(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project expenses", data));
    }

    // ADD EXPENSE
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ExpenseDto>> add(
            @RequestBody ExpenseDto request) {
        ExpenseDto data = expenseService.addExpense(request);
        return ResponseEntity.ok(ApiResponse.success("Expense added", data));
    }

    // UPDATE EXPENSE
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ExpenseDto>> update(
            @PathVariable Long id,
            @RequestBody ExpenseDto request) {
        ExpenseDto data = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated", data));
    }

    // DELETE EXPENSE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted", null));
    }
}