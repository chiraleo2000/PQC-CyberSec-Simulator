package com.pqc.document.repository;

import com.pqc.document.entity.Document;
import com.pqc.document.entity.Document.DocumentStatus;
import com.pqc.document.entity.Document.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByDocumentId(String documentId);

    List<Document> findByApplicantId(Long applicantId);

    List<Document> findByApplicantUserId(String userId);

    List<Document> findBySignerId(Long signerId);

    List<Document> findByStatus(DocumentStatus status);

    List<Document> findByDocumentType(DocumentType type);

    List<Document> findByApplicantIdAndStatus(Long applicantId, DocumentStatus status);
}
