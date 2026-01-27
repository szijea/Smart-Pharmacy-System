package com.pharmacy.repository;

import com.pharmacy.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT e FROM Employee e WHERE e.status = 1")
    List<Employee> findAllActive();

    @Query("SELECT e FROM Employee e WHERE e.roleId = :roleId AND e.status = 1")
    List<Employee> findByRoleId(@Param("roleId") Integer roleId);
}