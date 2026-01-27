// SettingRepository.java
package com.pharmacy.repository;

import com.pharmacy.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    @Query("SELECT s FROM Setting s WHERE s.id = (SELECT MAX(s2.id) FROM Setting s2)")
    Setting findLatestSettings();
}