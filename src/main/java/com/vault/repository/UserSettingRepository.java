package com.vault.repository;

import com.vault.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {

    List<UserSetting> findByUserId(Long userId);

    Optional<UserSetting> findByUserIdAndSettingName(Long userId, String settingName);
}
