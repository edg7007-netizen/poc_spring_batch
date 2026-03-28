package com.example.phonebatch.repository;

import com.example.phonebatch.domain.FeatureStoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeatureStoreRepository extends JpaRepository<FeatureStoreEntry, Long> {
    Optional<FeatureStoreEntry> findTopByPhoneNumberOrderByJobDateDesc(String phoneNumber);
}
