package com.designAccent.controller;

import com.designAccent.dto.ApiResponse;
import com.designAccent.dto.ProjectDto;
import com.designAccent.dto.ProjectSummaryDto;
import com.designAccent.entity.Project;
import com.designAccent.entity.ProjectCategory;
import com.designAccent.repository.ProjectCategoryRepository;
import com.designAccent.repository.ProjectRepository;
import com.designAccent.service.ProjectService;
import com.designAccent.exception.ResourceNotFoundException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    // CREATE PROJECT
    @PostMapping("/api/projects/create")
    public ResponseEntity<ApiResponse<ProjectDto>> create(
            @RequestBody ProjectDto request,
            @RequestParam Long ownerId) {
        ProjectDto data = projectService.createProject(request, ownerId);
        return ResponseEntity.ok(ApiResponse.success("Project created", data));
    }

    // GET ALL
    @GetMapping("/api/projects/all")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getAll() {
        List<ProjectDto> data = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponse.success("All projects", data));
    }

    // GET MY PROJECTS
    @GetMapping("/api/projects/my/{userId}")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getMyProjects(
            @PathVariable Long userId) {
        List<ProjectDto> data = projectService.getMyProjects(userId);
        return ResponseEntity.ok(ApiResponse.success("My projects", data));
    }

    // GET BY ID
    @GetMapping("/api/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getById(
            @PathVariable Long id) {
        ProjectDto data = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success("Project found", data));
    }

    // UPDATE
    @PutMapping("/api/projects/update/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> update(
            @PathVariable Long id,
            @RequestBody ProjectDto request) {
        ProjectDto data = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated", data));
    }

    // GET SUMMARY
    @GetMapping("/api/projects/summary")
    public ResponseEntity<ApiResponse<ProjectSummaryDto>> getSummary(
            @RequestParam(required = false) String status) {
        ProjectSummaryDto data = projectService.getProjectSummary(status);
        return ResponseEntity.ok(ApiResponse.success("Project summary", data));
    }

    // DELETE
    @DeleteMapping("/api/projects/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }

    // ADD CATEGORY — handles both new category and sub item
    // URL: POST /api/project-categories/add
    @PostMapping("/api/project-categories/add")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addCategory(
            @RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Name is required"));
        }

        ProjectCategory category = new ProjectCategory();
        category.setName(name);
        category.setIsGlobal(false);

        // set project if projectId provided
        if (request.get("projectId") != null) {
            Long projectId = Long.valueOf(
                    request.get("projectId").toString());
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Project not found"));
            category.setProject(project);
        }

        // set parent category if parentCategoryId provided (for sub items)
        if (request.get("parentCategoryId") != null) {
            Long parentId = Long.valueOf(
                    request.get("parentCategoryId").toString());
            ProjectCategory parent = categoryRepository.findById(parentId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }

        ProjectCategory saved = categoryRepository.save(category);

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("name", saved.getName());

        return ResponseEntity.ok(
                ApiResponse.success("Category added successfully", data));
    }
}