package com.pharmacy.service;

import com.pharmacy.entity.Prescription;

import java.util.List;

public interface PrescriptionService {
    List<Prescription> findAll();
    Prescription findById(String prescriptionId);
    List<Prescription> findByPatientName(String patientName);
    List<Prescription> findByVerifyStatus(Integer verifyStatus);
    List<Prescription> findTodayPrescriptions();
    Prescription save(Prescription prescription);
    Prescription verifyPrescription(String prescriptionId, Integer status, String remark);
}