package com.vault.repository;

import com.vault.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {

    List<UserDocument> findByUserId(Long userId);

    List<UserDocument> findByUserIdAndDocType(Long userId, String docType);
}
