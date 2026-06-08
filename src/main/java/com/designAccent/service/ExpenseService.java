package com.designAccent.service;

import com.designAccent.dto.ExpenseDto;
import com.designAccent.entity.Expense;
import com.designAccent.entity.Project;
import com.designAccent.entity.ProjectCategory;
import com.designAccent.exception.BadRequestException;
import com.designAccent.exception.ResourceNotFoundException;
import com.designAccent.repository.ExpenseRepository;
import com.designAccent.repository.ProjectCategoryRepository;
import com.designAccent.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    // GET ALL CATEGORIES WITH EXPENSES FOR A PROJECT
    public List<ExpenseDto> getProjectExpenses(Long projectId) {

        projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // get all parent categories
        List<ProjectCategory> parents = categoryRepository.findByParentIsNull();

        // get all expenses for this project
        List<Expense> allExpenses = expenseRepository.findByProjectId(projectId);

        List<ExpenseDto> result = new ArrayList<>();

        for (ProjectCategory parent : parents) {

            ExpenseDto parentDto = new ExpenseDto();
            parentDto.setCategoryId(parent.getId());
            parentDto.setCategoryName(parent.getName());

            // get subcategories under this parent
            List<ProjectCategory> subs = categoryRepository.findByParentId(parent.getId());

            List<ExpenseDto> subList = new ArrayList<>();

            for (ProjectCategory sub : subs) {

                ExpenseDto subDto = new ExpenseDto();
                subDto.setCategoryId(sub.getId());
                subDto.setCategoryName(sub.getName());
                subDto.setParentCategoryId(parent.getId());
                subDto.setParentCategoryName(parent.getName());

                // find expense for this subcategory in this project
                allExpenses.stream()
                    .filter(e -> e.getCategory().getId().equals(sub.getId()))
                    .findFirst()
                    .ifPresent(e -> {
                        subDto.setId(e.getId());
                        subDto.setProjectId(projectId);
                        subDto.setAmount(e.getAmount());
                        subDto.setTotalCost(e.getTotalCost());
                    });

                subList.add(subDto);
            }

            parentDto.setSubcategories(subList);
            result.add(parentDto);
        }

        return result;
    }

    // ADD EXPENSE TO SUBCATEGORY
    @Transactional
    public ExpenseDto addExpense(ExpenseDto request) {

        if (request.getProjectId() == null) {
            throw new BadRequestException("Project id is required");
        }
        if (request.getCategoryId() == null) {
            throw new BadRequestException("Category id is required");
        }

        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        ProjectCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // check if expense already exists
        List<Expense> existing = expenseRepository
            .findByProjectIdAndCategoryId(request.getProjectId(), request.getCategoryId());
        if (!existing.isEmpty()) {
            throw new BadRequestException("Expense already exists for this category — use update instead");
        }

        Expense expense = new Expense();
        expense.setProject(project);
        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setTotalCost(request.getTotalCost());
        expense.setCreatedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(expense);

        return mapToDto(saved);
    }

    // UPDATE EXPENSE
    @Transactional
    public ExpenseDto updateExpense(Long id, ExpenseDto request) {

        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getTotalCost() != null) expense.setTotalCost(request.getTotalCost());

        Expense updated = expenseRepository.save(expense);
        return mapToDto(updated);
    }

    // DELETE EXPENSE
    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    // MAP entity to dto
    private ExpenseDto mapToDto(Expense expense) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setProjectId(expense.getProject().getId());
        dto.setCategoryId(expense.getCategory().getId());
        dto.setCategoryName(expense.getCategory().getName());
        dto.setAmount(expense.getAmount());
        dto.setTotalCost(expense.getTotalCost());
        if (expense.getCategory().getParent() != null) {
            dto.setParentCategoryId(expense.getCategory().getParent().getId());
            dto.setParentCategoryName(expense.getCategory().getParent().getName());
        }
        return dto;
    }
}