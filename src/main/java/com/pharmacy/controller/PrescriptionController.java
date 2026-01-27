package com.pharmacy.controller;

import com.pharmacy.entity.Prescription;
import com.pharmacy.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @GetMapping
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        List<Prescription> prescriptions = prescriptionService.findAll();
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/{prescriptionId}")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable String prescriptionId) {
        Prescription prescription = prescriptionService.findById(prescriptionId);
        if (prescription != null) {
            return new ResponseEntity<>(prescription, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Prescription>> searchPrescriptions(@RequestParam String patientName) {
        List<Prescription> prescriptions = prescriptionService.findByPatientName(patientName);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Prescription>> getPrescriptionsByStatus(@PathVariable Integer status) {
        List<Prescription> prescriptions = prescriptionService.findByVerifyStatus(status);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createPrescription(@RequestBody Prescription prescription) {
        try {
            Prescription saved = prescriptionService.save(prescription);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{prescriptionId}/verify")
    public ResponseEntity<?> verifyPrescription(@PathVariable String prescriptionId,
                                                @RequestBody Map<String, Object> verifyData) {
        try {
            Integer status = (Integer) verifyData.get("status");
            String remark = (String) verifyData.get("remark");

            Prescription prescription = prescriptionService.verifyPrescription(prescriptionId, status, remark);
            return new ResponseEntity<>(prescription, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/today")
    public ResponseEntity<List<Prescription>> getTodayPrescriptions() {
        List<Prescription> prescriptions = prescriptionService.findTodayPrescriptions();
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }
}