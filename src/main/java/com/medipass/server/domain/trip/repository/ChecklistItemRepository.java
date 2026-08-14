package com.medipass.server.domain.trip.repository;

import com.medipass.server.domain.trip.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
}
