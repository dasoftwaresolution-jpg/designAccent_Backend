//package com.designAccent.repository;
//
//import com.designAccent.entity.Expense;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//
//@Repository
//public interface ExpenseRepository extends JpaRepository<Expense, Long> {
//    List<Expense> findByProjectId(Long projectId);
//    List<Expense> findByProjectIdAndCategoryId(Long projectId, Long categoryId);
//}

package com.designAccent.repository;

import com.designAccent.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByProjectId(Long projectId);
    List<Expense> findByProjectIdAndCategoryId(Long projectId, Long categoryId);

    // total outflow - all time
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAllAmount();

    // total outflow - by month
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE EXTRACT(MONTH FROM e.createdAt) = :month AND EXTRACT(YEAR FROM e.createdAt) = :year")
    BigDecimal sumAmountByMonth(@Param("month") int month, @Param("year") int year);
}