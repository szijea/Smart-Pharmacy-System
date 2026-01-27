// Setting.java
package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_phone")
    private String storePhone;

    @Column(name = "store_address")
    private String storeAddress;

    @Column(name = "store_desc")
    private String storeDesc;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 10;

    @Column(name = "notify_methods")
    private String notifyMethods;

    @Column(name = "points_rule")
    private Double pointsRule = 1.0;

    @Column(name = "cash_rule")
    private Integer cashRule = 100;

    @Column(name = "operation_log")
    private Boolean operationLog = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "open_time", length = 10)
    private String openTime;

    @Column(name = "close_time", length = 10)
    private String closeTime;

    @Column(name = "license_number", length = 64)
    private String licenseNumber;

    @Column(name = "notify_system")
    private Boolean notifySystem = true;

    @Column(name = "notify_email")
    private Boolean notifyEmail = false;

    @Column(name = "notify_sms")
    private Boolean notifySms = true;

    @Column(name = "notify_email_address", length = 128)
    private String notifyEmailAddress;

    @Column(name = "email_frequency", length = 20)
    private String emailFrequency;

    @Column(name = "notify_phone", length = 20)
    private String notifyPhone;

    @Column(name = "sms_frequency", length = 20)
    private String smsFrequency;

    @Column(name = "inventory_check_cycle", length = 20)
    private String inventoryCheckCycle;

    @Column(name = "normal_to_silver")
    private Integer normalToSilver = 1000;

    @Column(name = "normal_discount")
    private Integer normalDiscount = 100; // 100% 不打折

    @Column(name = "silver_to_gold")
    private Integer silverToGold = 5000;

    @Column(name = "silver_discount")
    private Integer silverDiscount = 95;

    @Column(name = "gold_to_platinum")
    private Integer goldToPlatinum = 10000;

    @Column(name = "gold_discount")
    private Integer goldDiscount = 90;

    @Column(name = "enable_cash")
    private Boolean enableCash = true;

    @Column(name = "enable_wechat")
    private Boolean enableWechat = true;

    @Column(name = "enable_alipay")
    private Boolean enableAlipay = true;

    @Column(name = "enable_member_card")
    private Boolean enableMemberCard = true;

    @Column(name = "wechat_mch_id", length = 64)
    private String wechatMchId;

    @Column(name = "wechat_api_key", length = 128)
    private String wechatApiKey;

    @Column(name = "alipay_app_id", length = 64)
    private String alipayAppId;

    @Column(name = "alipay_private_key", columnDefinition = "TEXT")
    private String alipayPrivateKey;

    @Column(name = "change_unit", length = 10)
    private String changeUnit = "0.10";

    @Column(name = "default_printer", length = 32)
    private String defaultPrinter = "local";

    @Column(name = "paper_size", length = 32)
    private String paperSize = "pos80";

    @Column(name = "print_logo")
    private Boolean printLogo = true;

    @Column(name = "print_qrcode")
    private Boolean printQrcode = true;

    @Column(name = "print_member")
    private Boolean printMember = true;

    @Column(name = "print_drug_detail")
    private Boolean printDrugDetail = true;

    @Column(name = "print_usage")
    private Boolean printUsage = true;

    @Column(name = "print_footer")
    private Boolean printFooter = false;

    @Column(name = "footer_text", length = 512)
    private String footerText;

    @Column(name = "receipt_copies")
    private Integer receiptCopies = 1;

    @Column(name = "prescription_copies")
    private Integer prescriptionCopies = 2;

    @Column(name = "enable_log")
    private Boolean enableLog = true;

    @Column(name = "log_retention")
    private Integer logRetention = 90;

    @Column(name = "pwd_expiry")
    private Integer pwdExpiry = 90;

    @Column(name = "login_attempts")
    private Integer loginAttempts = 5;

    @Column(name = "enable_ip_restrict")
    private Boolean enableIpRestrict = false;

    @Column(name = "auto_backup")
    private Boolean autoBackup = true;

    @Column(name = "backup_cycle", length = 20)
    private String backupCycle = "weekly";

    @Column(name = "backup_retention")
    private Integer backupRetention = 5;

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public void setStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStoreDesc() {
        return storeDesc;
    }

    public void setStoreDesc(String storeDesc) {
        this.storeDesc = storeDesc;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getNotifyMethods() {
        return notifyMethods;
    }

    public void setNotifyMethods(String notifyMethods) {
        this.notifyMethods = notifyMethods;
    }

    public Double getPointsRule() {
        return pointsRule;
    }

    public void setPointsRule(Double pointsRule) {
        this.pointsRule = pointsRule;
    }

    public Integer getCashRule() {
        return cashRule;
    }

    public void setCashRule(Integer cashRule) {
        this.cashRule = cashRule;
    }

    public Boolean getOperationLog() {
        return operationLog;
    }

    public void setOperationLog(Boolean operationLog) {
        this.operationLog = operationLog;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public Boolean getNotifySystem() { return notifySystem; }
    public void setNotifySystem(Boolean notifySystem) { this.notifySystem = notifySystem; }
    public Boolean getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(Boolean notifyEmail) { this.notifyEmail = notifyEmail; }
    public Boolean getNotifySms() { return notifySms; }
    public void setNotifySms(Boolean notifySms) { this.notifySms = notifySms; }
    public String getNotifyEmailAddress() { return notifyEmailAddress; }
    public void setNotifyEmailAddress(String notifyEmailAddress) { this.notifyEmailAddress = notifyEmailAddress; }
    public String getEmailFrequency() { return emailFrequency; }
    public void setEmailFrequency(String emailFrequency) { this.emailFrequency = emailFrequency; }
    public String getNotifyPhone() { return notifyPhone; }
    public void setNotifyPhone(String notifyPhone) { this.notifyPhone = notifyPhone; }
    public String getSmsFrequency() { return smsFrequency; }
    public void setSmsFrequency(String smsFrequency) { this.smsFrequency = smsFrequency; }
    public String getInventoryCheckCycle() { return inventoryCheckCycle; }
    public void setInventoryCheckCycle(String inventoryCheckCycle) { this.inventoryCheckCycle = inventoryCheckCycle; }
    public Integer getNormalToSilver() { return normalToSilver; }
    public void setNormalToSilver(Integer normalToSilver) { this.normalToSilver = normalToSilver; }
    public Integer getNormalDiscount() { return normalDiscount; }
    public void setNormalDiscount(Integer normalDiscount) { this.normalDiscount = normalDiscount; }
    public Integer getSilverToGold() { return silverToGold; }
    public void setSilverToGold(Integer silverToGold) { this.silverToGold = silverToGold; }
    public Integer getSilverDiscount() { return silverDiscount; }
    public void setSilverDiscount(Integer silverDiscount) { this.silverDiscount = silverDiscount; }
    public Integer getGoldToPlatinum() { return goldToPlatinum; }
    public void setGoldToPlatinum(Integer goldToPlatinum) { this.goldToPlatinum = goldToPlatinum; }
    public Integer getGoldDiscount() { return goldDiscount; }
    public void setGoldDiscount(Integer goldDiscount) { this.goldDiscount = goldDiscount; }
    public Boolean getEnableCash() { return enableCash; }
    public void setEnableCash(Boolean enableCash) { this.enableCash = enableCash; }
    public Boolean getEnableWechat() { return enableWechat; }
    public void setEnableWechat(Boolean enableWechat) { this.enableWechat = enableWechat; }
    public Boolean getEnableAlipay() { return enableAlipay; }
    public void setEnableAlipay(Boolean enableAlipay) { this.enableAlipay = enableAlipay; }
    public Boolean getEnableMemberCard() { return enableMemberCard; }
    public void setEnableMemberCard(Boolean enableMemberCard) { this.enableMemberCard = enableMemberCard; }
    public String getWechatMchId() { return wechatMchId; }
    public void setWechatMchId(String wechatMchId) { this.wechatMchId = wechatMchId; }
    public String getWechatApiKey() { return wechatApiKey; }
    public void setWechatApiKey(String wechatApiKey) { this.wechatApiKey = wechatApiKey; }
    public String getAlipayAppId() { return alipayAppId; }
    public void setAlipayAppId(String alipayAppId) { this.alipayAppId = alipayAppId; }
    public String getAlipayPrivateKey() { return alipayPrivateKey; }
    public void setAlipayPrivateKey(String alipayPrivateKey) { this.alipayPrivateKey = alipayPrivateKey; }
    public String getChangeUnit() { return changeUnit; }
    public void setChangeUnit(String changeUnit) { this.changeUnit = changeUnit; }
    public String getDefaultPrinter() { return defaultPrinter; }
    public void setDefaultPrinter(String defaultPrinter) { this.defaultPrinter = defaultPrinter; }
    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }
    public Boolean getPrintLogo() { return printLogo; }
    public void setPrintLogo(Boolean printLogo) { this.printLogo = printLogo; }
    public Boolean getPrintQrcode() { return printQrcode; }
    public void setPrintQrcode(Boolean printQrcode) { this.printQrcode = printQrcode; }
    public Boolean getPrintMember() { return printMember; }
    public void setPrintMember(Boolean printMember) { this.printMember = printMember; }
    public Boolean getPrintDrugDetail() { return printDrugDetail; }
    public void setPrintDrugDetail(Boolean printDrugDetail) { this.printDrugDetail = printDrugDetail; }
    public Boolean getPrintUsage() { return printUsage; }
    public void setPrintUsage(Boolean printUsage) { this.printUsage = printUsage; }
    public Boolean getPrintFooter() { return printFooter; }
    public void setPrintFooter(Boolean printFooter) { this.printFooter = printFooter; }
    public String getFooterText() { return footerText; }
    public void setFooterText(String footerText) { this.footerText = footerText; }
    public Integer getReceiptCopies() { return receiptCopies; }
    public void setReceiptCopies(Integer receiptCopies) { this.receiptCopies = receiptCopies; }
    public Integer getPrescriptionCopies() { return prescriptionCopies; }
    public void setPrescriptionCopies(Integer prescriptionCopies) { this.prescriptionCopies = prescriptionCopies; }
    public Boolean getEnableLog() { return enableLog; }
    public void setEnableLog(Boolean enableLog) { this.enableLog = enableLog; }
    public Integer getLogRetention() { return logRetention; }
    public void setLogRetention(Integer logRetention) { this.logRetention = logRetention; }
    public Integer getPwdExpiry() { return pwdExpiry; }
    public void setPwdExpiry(Integer pwdExpiry) { this.pwdExpiry = pwdExpiry; }
    public Integer getLoginAttempts() { return loginAttempts; }
    public void setLoginAttempts(Integer loginAttempts) { this.loginAttempts = loginAttempts; }
    public Boolean getEnableIpRestrict() { return enableIpRestrict; }
    public void setEnableIpRestrict(Boolean enableIpRestrict) { this.enableIpRestrict = enableIpRestrict; }
    public Boolean getAutoBackup() { return autoBackup; }
    public void setAutoBackup(Boolean autoBackup) { this.autoBackup = autoBackup; }
    public String getBackupCycle() { return backupCycle; }
    public void setBackupCycle(String backupCycle) { this.backupCycle = backupCycle; }
    public Integer getBackupRetention() { return backupRetention; }
    public void setBackupRetention(Integer backupRetention) { this.backupRetention = backupRetention; }
}

