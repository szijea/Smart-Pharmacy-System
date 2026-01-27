package com.pharmacy.service.impl;

import com.pharmacy.entity.Prescription;
import com.pharmacy.repository.PrescriptionRepository;
import com.pharmacy.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Override
    public List<Prescription> findAll() {
        return prescriptionRepository.findAll();
    }

    @Override
    public Prescription findById(String prescriptionId) {
        return prescriptionRepository.findById(prescriptionId).orElse(null);
    }

    @Override
    public List<Prescription> findByPatientName(String patientName) {
        return prescriptionRepository.findByPatientNameContaining(patientName);
    }

    @Override
    public List<Prescription> findByVerifyStatus(Integer verifyStatus) {
        return prescriptionRepository.findByVerifyStatus(verifyStatus);
    }

    @Override
    public List<Prescription> findTodayPrescriptions() {
        return prescriptionRepository.findTodayPrescriptions();
    }

    @Override
    @Transactional
    public Prescription save(Prescription prescription) {
        if (prescription.getPrescriptionId() == null) {
            // 生成处方ID
            String prescriptionId = "P" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
            prescription.setPrescriptionId(prescriptionId);
        }

        if (prescription.getPrescriptionDate() == null) {
            prescription.setPrescriptionDate(LocalDate.now());
        }

        return prescriptionRepository.save(prescription);
    }

    @Override
    @Transactional
    public Prescription verifyPrescription(String prescriptionId, Integer status, String remark) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("处方不存在"));

        prescription.setVerifyStatus(status);
        prescription.setVerifyRemark(remark);

        return prescriptionRepository.save(prescription);
    }
}