package com.pharmacy.repository;

import com.pharmacy.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

    List<Prescription> findByPatientNameContaining(String patientName);

    List<Prescription> findByVerifyStatus(Integer verifyStatus);

    @Query("SELECT p FROM Prescription p WHERE p.prescriptionDate = CURRENT_DATE")
    List<Prescription> findTodayPrescriptions();
}