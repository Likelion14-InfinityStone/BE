package com.medipass.server.domain.document.repository;

import com.medipass.server.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByChecklistItem_Id(Long checklistItemId);

    boolean existsByChecklistItem_Id(Long checklistItemId);
}
