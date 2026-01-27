// SettingController.java
package com.pharmacy.controller;

import com.pharmacy.entity.Setting;
import com.pharmacy.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
// 使用全局 CORS 配置
public class SettingController {

    @Autowired
    private SettingRepository settingRepository;

    @GetMapping
    public ResponseEntity<?> getSettings() {
        // 前端 settings.html 直接期望一个扁平对象包含 storeName 等字段
        Setting settings = null;
        try {
            settings = settingRepository.findLatestSettings();
        } catch (Exception ex) {
            // 表不存在或查询异常，退回默认内存对象，不再返回 500
            System.err.println("[SettingController] 查询设置异常, 使用内存默认值: " + ex.getMessage());
        }
        if (settings == null) {
            settings = new Setting();
            settings.setCreatedAt(LocalDateTime.now());
            settings.setUpdatedAt(LocalDateTime.now());
            settings.setStoreName("智慧药房");
            settings.setStorePhone("400-000-0000");
            settings.setStoreAddress("未设置地址");
            settings.setStoreDesc("首次初始化默认配置");
            // 若数据库正常可保存初始化记录；若表缺失会触发异常直接忽略
            try { settingRepository.save(settings); } catch (Exception ignore) {}
        }
        // 返回扁平 Map（不含 code/message），兼容旧前端逻辑
        return ResponseEntity.ok(buildSettingsMap(settings));
    }

    @GetMapping("/raw")
    public ResponseEntity<?> getRawEntity(){
        Setting s = settingRepository.findLatestSettings();
        return ResponseEntity.ok(Map.of("code",200,"data", s));
    }

    @PostMapping
    public ResponseEntity<?> updateSettings(@RequestBody Map<String,Object> settingsData) {
        try {

            Setting settings = settingRepository.findLatestSettings();
            if (settings == null) {
                settings = new Setting();
                settings.setCreatedAt(LocalDateTime.now());
            }
            // 基础字段更新（安全转换）
            putIfString(settingsData, "storeName", settings::setStoreName);
            putIfString(settingsData, "storePhone", settings::setStorePhone);
            putIfString(settingsData, "storeAddress", settings::setStoreAddress);
            putIfString(settingsData, "storeDesc", settings::setStoreDesc);
            putIfInt(settingsData, "lowStockThreshold", settings::setLowStockThreshold);
            putIfDouble(settingsData, "pointsRule", settings::setPointsRule);
            putIfInt(settingsData, "cashRule", settings::setCashRule);
            putIfBool(settingsData, "operationLog", settings::setOperationLog);
            // 时间 & 许可证
            putIfString(settingsData, "openTime", settings::setOpenTime);
            putIfString(settingsData, "closeTime", settings::setCloseTime);
            putIfString(settingsData, "licenseNumber", settings::setLicenseNumber);
            // 通知设置
            putIfBool(settingsData, "notifySystem", settings::setNotifySystem);
            putIfBool(settingsData, "notifyEmail", settings::setNotifyEmail);
            putIfBool(settingsData, "notifySms", settings::setNotifySms);
            putIfString(settingsData, "notifyEmailAddress", settings::setNotifyEmailAddress);
            putIfString(settingsData, "emailFrequency", settings::setEmailFrequency);
            putIfString(settingsData, "notifyPhone", settings::setNotifyPhone);
            putIfString(settingsData, "smsFrequency", settings::setSmsFrequency);
            putIfString(settingsData, "inventoryCheckCycle", settings::setInventoryCheckCycle);
            // 会员等级
            putIfInt(settingsData, "normalToSilver", settings::setNormalToSilver);
            putIfInt(settingsData, "normalDiscount", settings::setNormalDiscount);
            putIfInt(settingsData, "silverToGold", settings::setSilverToGold);
            putIfInt(settingsData, "silverDiscount", settings::setSilverDiscount);
            putIfInt(settingsData, "goldToPlatinum", settings::setGoldToPlatinum);
            putIfInt(settingsData, "goldDiscount", settings::setGoldDiscount);
            // 支付方式
            putIfBool(settingsData, "enableCash", settings::setEnableCash);
            putIfBool(settingsData, "enableWechat", settings::setEnableWechat);
            putIfBool(settingsData, "enableAlipay", settings::setEnableAlipay);
            putIfBool(settingsData, "enableMemberCard", settings::setEnableMemberCard);
            putIfString(settingsData, "wechatMchId", settings::setWechatMchId);
            putIfString(settingsData, "wechatApiKey", settings::setWechatApiKey);
            putIfString(settingsData, "alipayAppId", settings::setAlipayAppId);
            putIfString(settingsData, "alipayPrivateKey", settings::setAlipayPrivateKey);
            // 打印 & 小票
            putIfString(settingsData, "changeUnit", settings::setChangeUnit);
            putIfString(settingsData, "defaultPrinter", settings::setDefaultPrinter);
            putIfString(settingsData, "paperSize", settings::setPaperSize);
            putIfBool(settingsData, "printLogo", settings::setPrintLogo);
            putIfBool(settingsData, "printQrcode", settings::setPrintQrcode);
            putIfBool(settingsData, "printMember", settings::setPrintMember);
            putIfBool(settingsData, "printDrugDetail", settings::setPrintDrugDetail);
            putIfBool(settingsData, "printUsage", settings::setPrintUsage);
            putIfBool(settingsData, "printFooter", settings::setPrintFooter);
            putIfString(settingsData, "footerText", settings::setFooterText);
            putIfInt(settingsData, "receiptCopies", settings::setReceiptCopies);
            putIfInt(settingsData, "prescriptionCopies", settings::setPrescriptionCopies);
            // 安全 & 日志
            putIfBool(settingsData, "enableLog", settings::setEnableLog);
            putIfInt(settingsData, "logRetention", settings::setLogRetention);
            putIfInt(settingsData, "pwdExpiry", settings::setPwdExpiry);
            putIfInt(settingsData, "loginAttempts", settings::setLoginAttempts);
            putIfBool(settingsData, "enableIpRestrict", settings::setEnableIpRestrict);
            // 备份
            putIfBool(settingsData, "autoBackup", settings::setAutoBackup);
            putIfString(settingsData, "backupCycle", settings::setBackupCycle);
            putIfInt(settingsData, "backupRetention", settings::setBackupRetention);

            settings.setUpdatedAt(LocalDateTime.now());
            settingRepository.save(settings);
            Map<String,Object> data = buildSettingsMap(settings);
            return ResponseEntity.ok(Map.of("code",200,"message","设置保存成功","data",data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","保存设置失败: "+e.getMessage()));
        }
    }

    private Map<String,Object> buildSettingsMap(Setting settings){
        Map<String,Object> m = new HashMap<>();
        m.put("storeName", settings.getStoreName());
        m.put("storePhone", settings.getStorePhone());
        m.put("storeAddress", settings.getStoreAddress());
        m.put("storeDesc", settings.getStoreDesc());
        m.put("lowStockThreshold", settings.getLowStockThreshold());
        m.put("pointsRule", settings.getPointsRule());
        m.put("cashRule", settings.getCashRule());
        m.put("operationLog", settings.getOperationLog());
        m.put("openTime", settings.getOpenTime());
        m.put("closeTime", settings.getCloseTime());
        m.put("licenseNumber", settings.getLicenseNumber());
        m.put("notifySystem", settings.getNotifySystem());
        m.put("notifyEmail", settings.getNotifyEmail());
        m.put("notifySms", settings.getNotifySms());
        m.put("notifyEmailAddress", settings.getNotifyEmailAddress());
        m.put("emailFrequency", settings.getEmailFrequency());
        m.put("notifyPhone", settings.getNotifyPhone());
        m.put("smsFrequency", settings.getSmsFrequency());
        m.put("inventoryCheckCycle", settings.getInventoryCheckCycle());
        m.put("normalToSilver", settings.getNormalToSilver());
        m.put("normalDiscount", settings.getNormalDiscount());
        m.put("silverToGold", settings.getSilverToGold());
        m.put("silverDiscount", settings.getSilverDiscount());
        m.put("goldToPlatinum", settings.getGoldToPlatinum());
        m.put("goldDiscount", settings.getGoldDiscount());
        m.put("enableCash", settings.getEnableCash());
        m.put("enableWechat", settings.getEnableWechat());
        m.put("enableAlipay", settings.getEnableAlipay());
        m.put("enableMemberCard", settings.getEnableMemberCard());
        m.put("wechatMchId", settings.getWechatMchId());
        m.put("wechatApiKey", settings.getWechatApiKey());
        m.put("alipayAppId", settings.getAlipayAppId());
        m.put("alipayPrivateKey", settings.getAlipayPrivateKey());
        m.put("changeUnit", settings.getChangeUnit());
        m.put("defaultPrinter", settings.getDefaultPrinter());
        m.put("paperSize", settings.getPaperSize());
        m.put("printLogo", settings.getPrintLogo());
        m.put("printQrcode", settings.getPrintQrcode());
        m.put("printMember", settings.getPrintMember());
        m.put("printDrugDetail", settings.getPrintDrugDetail());
        m.put("printUsage", settings.getPrintUsage());
        m.put("printFooter", settings.getPrintFooter());
        m.put("footerText", settings.getFooterText());
        m.put("receiptCopies", settings.getReceiptCopies());
        m.put("prescriptionCopies", settings.getPrescriptionCopies());
        m.put("enableLog", settings.getEnableLog());
        m.put("logRetention", settings.getLogRetention());
        m.put("pwdExpiry", settings.getPwdExpiry());
        m.put("loginAttempts", settings.getLoginAttempts());
        m.put("enableIpRestrict", settings.getEnableIpRestrict());
        m.put("autoBackup", settings.getAutoBackup());
        m.put("backupCycle", settings.getBackupCycle());
        m.put("backupRetention", settings.getBackupRetention());
        return m;
    }

    private void putIfString(Map<String,Object> src, String key, java.util.function.Consumer<String> setter){
        if(src.containsKey(key) && src.get(key)!=null){ setter.accept(String.valueOf(src.get(key))); }
    }
    private void putIfInt(Map<String,Object> src, String key, java.util.function.Consumer<Integer> setter){
        if(src.containsKey(key) && src.get(key)!=null){ setter.accept(Integer.valueOf(src.get(key).toString())); }
    }
    private void putIfDouble(Map<String,Object> src, String key, java.util.function.Consumer<Double> setter){
        if(src.containsKey(key) && src.get(key)!=null){ setter.accept(Double.valueOf(src.get(key).toString())); }
    }
    private void putIfBool(Map<String,Object> src, String key, java.util.function.Consumer<Boolean> setter){
        if(src.containsKey(key) && src.get(key)!=null){ setter.accept(Boolean.valueOf(src.get(key).toString())); }
    }
}