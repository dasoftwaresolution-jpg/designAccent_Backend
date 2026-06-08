package com.designAccent.repository;

import com.designAccent.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // get projects where user is owner
    List<Project> findByOwnerId(Long ownerId);

    // get projects where user is a member
    @Query("SELECT p FROM Project p JOIN ProjectUser pu ON p.id = pu.project.id WHERE pu.user.id = :userId")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);

    // total inflow - all time
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Project p")
    BigDecimal sumTotalAmount();

    // total inflow - by month
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Project p WHERE EXTRACT(MONTH FROM p.createdAt) = :month AND EXTRACT(YEAR FROM p.createdAt) = :year")
    BigDecimal sumTotalAmountByMonth(@Param("month") int month, @Param("year") int year);

    // total budget - by month
    @Query("SELECT COALESCE(SUM(p.totalBudget), 0) FROM Project p WHERE EXTRACT(MONTH FROM p.createdAt) = :month AND EXTRACT(YEAR FROM p.createdAt) = :year")
    BigDecimal sumTotalBudgetByMonth(@Param("month") int month, @Param("year") int year);

    // vendor dues - all time
    @Query("SELECT COALESCE(SUM(p.totalBudget - p.totalAmount), 0) FROM Project p WHERE p.totalBudget > p.totalAmount")
    BigDecimal sumVendorDues();

    // vendor dues - by month
    @Query("SELECT COALESCE(SUM(p.totalBudget - p.totalAmount), 0) FROM Project p WHERE p.totalBudget > p.totalAmount AND EXTRACT(MONTH FROM p.createdAt) = :month AND EXTRACT(YEAR FROM p.createdAt) = :year")
    BigDecimal sumVendorDuesByMonth(@Param("month") int month, @Param("year") int year);

    // count by status
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    Long countByStatus(@Param("status") String status);

    // filter by status
    List<Project> findByStatus(String status);
    
    // sort: active first, hold second, completed last
//    @Query("SELECT p FROM Project p ORDER BY " +
//           "CASE p.status " +
//           "WHEN 'active' THEN 1 " +
//           "WHEN 'hold' THEN 2 " +
//           "WHEN 'completed' THEN 3 " +
//           "ELSE 4 END ASC")
//    List<Project> findAllOrderByStatus();
//
//    // with status filter + sort
//    @Query("SELECT p FROM Project p WHERE " +
//           "(:status IS NULL OR :status = '' OR p.status = :status) " +
//           "ORDER BY " +
//           "CASE p.status " +
//           "WHEN 'active' THEN 1 " +
//           "WHEN 'hold' THEN 2 " +
//           "WHEN 'completed' THEN 3 " +
//           "ELSE 4 END ASC")
//    List<Project> findAllByStatusOrderByStatus(
//            @Param("status") String status);
    
    
}