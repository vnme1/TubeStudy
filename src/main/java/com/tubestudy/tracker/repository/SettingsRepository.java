package com.tubestudy.tracker.repository;

import com.tubestudy.tracker.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Long> {

}