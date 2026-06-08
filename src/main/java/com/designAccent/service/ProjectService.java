package com.designAccent.service;

import com.designAccent.dto.ProjectDto;
import com.designAccent.dto.ProjectSummaryDto;
import com.designAccent.entity.Project;
import com.designAccent.entity.ProjectUser;
import com.designAccent.entity.User;
import com.designAccent.exception.BadRequestException;
import com.designAccent.exception.ResourceNotFoundException;
import com.designAccent.repository.ProjectRepository;
import com.designAccent.repository.ProjectUserRepository;
import com.designAccent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ProjectDto createProject(ProjectDto request, Long ownerId) {
        if (request.getProjectName() == null || request.getProjectName().isEmpty()) {
            throw new BadRequestException("Project name is required");
        }
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Project project = new Project();
        project.setOwner(owner);
        project.setProjectName(request.getProjectName());
        project.setClientName(request.getClientName());
        project.setProjectType(request.getProjectType());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        project.setLocation(request.getLocation());
        project.setCashAmount(request.getCashAmount());
        project.setOnlineAmount(request.getOnlineAmount());
        project.setAccountAmount(request.getAccountAmount());
        project.setTotalBudget(request.getTotalBudget());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProject(saved);
        projectUser.setUser(owner);
        projectUserRepository.save(projectUser);
        return mapToDto(saved);
    }

    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll()
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ProjectDto> getMyProjects(Long userId) {
        return projectRepository.findProjectsByUserId(userId)
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return mapToDto(project);
    }

    @Transactional
    public ProjectDto updateProject(Long id, ProjectDto request) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (request.getProjectName() != null) project.setProjectName(request.getProjectName());
        if (request.getClientName() != null) project.setClientName(request.getClientName());
        if (request.getProjectType() != null) project.setProjectType(request.getProjectType());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getLocation() != null) project.setLocation(request.getLocation());
        if (request.getCashAmount() != null) project.setCashAmount(request.getCashAmount());
        if (request.getOnlineAmount() != null) project.setOnlineAmount(request.getOnlineAmount());
        if (request.getAccountAmount() != null) project.setAccountAmount(request.getAccountAmount());
        if (request.getTotalBudget() != null) project.setTotalBudget(request.getTotalBudget());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
        return mapToDto(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        projectRepository.delete(project);
    }

    public ProjectSummaryDto getProjectSummary(String status) {
        int month = LocalDateTime.now().getMonthValue();
        int year  = LocalDateTime.now().getYear();

        BigDecimal totalValue    = projectRepository.sumTotalBudgetByMonth(month, year);
        BigDecimal totalReceived = projectRepository.sumTotalAmountByMonth(month, year);
        BigDecimal totalDues     = totalValue.subtract(totalReceived);

        Long activeCount = projectRepository.countByStatus("active");
        Long holdCount   = projectRepository.countByStatus("hold");
        Long doneCount   = projectRepository.countByStatus("completed");

        List<Project> projects = (status != null && !status.isEmpty())
            ? projectRepository.findByStatus(status)
            : projectRepository.findAll();

        ProjectSummaryDto summary = new ProjectSummaryDto();
        summary.setTotalValue(totalValue);
        summary.setTotalReceived(totalReceived);
        summary.setTotalDues(totalDues);
        summary.setActiveCount(activeCount);
        summary.setHoldCount(holdCount);
        summary.setDoneCount(doneCount);
        summary.setProjects(
            projects.stream().map(this::mapToDto).collect(Collectors.toList())
        );
        return summary;
    }

    private ProjectDto mapToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setProjectName(project.getProjectName());
        dto.setClientName(project.getClientName());
        dto.setProjectType(project.getProjectType());
        dto.setStatus(project.getStatus());
        dto.setLocation(project.getLocation());
        dto.setCashAmount(project.getCashAmount());
        dto.setOnlineAmount(project.getOnlineAmount());
        dto.setAccountAmount(project.getAccountAmount());
        dto.setTotalAmount(project.getTotalAmount());
        dto.setTotalBudget(project.getTotalBudget());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setCreatedAt(project.getCreatedAt());
        if (project.getTotalBudget() != null && project.getTotalAmount() != null) {
            dto.setVendorDues(project.getTotalBudget().subtract(project.getTotalAmount()));
        }
        if (project.getTotalBudget() != null
                && project.getTotalAmount() != null
                && project.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            int percent = project.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(project.getTotalBudget(), 0, RoundingMode.HALF_UP)
                .intValue();
            dto.setCompletionPercent(percent);
        }
        if (project.getOwner() != null) {
            dto.setOwnerId(project.getOwner().getId());
            dto.setOwnerName(project.getOwner().getName());
        }
        return dto;
    }
}