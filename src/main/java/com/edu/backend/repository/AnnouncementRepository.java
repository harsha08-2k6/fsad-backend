package com.edu.backend.repository;

import com.edu.backend.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, String> {
    List<Announcement> findByTargetRole(String targetRole);
}
