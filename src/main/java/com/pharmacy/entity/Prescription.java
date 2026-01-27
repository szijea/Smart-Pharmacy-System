package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescription")
public class Prescription {
    @Id
    @Column(name = "prescription_id", length = 32)
    private String prescriptionId;

    @Column(name = "patient_name", length = 50)
    private String patientName;

    @Column(name = "doctor_name", length = 50)
    private String doctorName;

    @Column(name = "hospital", length = 100)
    private String hospital;

    @Column(name = "prescription_date")
    private LocalDate prescriptionDate;

    @Column(name = "img_url", nullable = false, length = 255)
    private String imgUrl;

    @Column(name = "verify_status")
    private Integer verifyStatus = 0;

    @Column(name = "verify_remark", length = 200)
    private String verifyRemark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 构造方法、Getter和Setter
    public Prescription() {}

    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }

    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public Integer getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(Integer verifyStatus) { this.verifyStatus = verifyStatus; }

    public String getVerifyRemark() { return verifyRemark; }
    public void setVerifyRemark(String verifyRemark) { this.verifyRemark = verifyRemark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}