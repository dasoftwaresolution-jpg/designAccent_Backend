package com.designAccent.repository;

import com.designAccent.entity.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, Long> {
    List<ProjectCategory> findByParentIsNull();
    List<ProjectCategory> findByParentId(Long parentId);
}