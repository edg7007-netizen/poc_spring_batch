package com.poc.springbatch.repository;

import com.poc.springbatch.entity.PhoneFeatureStoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for the phone feature store.
 */
@Repository
public interface PhoneFeatureStoreRepository extends JpaRepository<PhoneFeatureStoreEntry, Long> {

    Optional<PhoneFeatureStoreEntry> findByPhoneNumberAndScoreDate(String phoneNumber, LocalDate scoreDate);

    Optional<PhoneFeatureStoreEntry> findTopByPhoneNumberOrderByScoreDateDesc(String phoneNumber);

    Optional<PhoneFeatureStoreEntry> findTopByUserIdOrderByScoreDateDesc(String userId);

    List<PhoneFeatureStoreEntry> findByPhoneNumberOrderByScoreDateDesc(String phoneNumber);

    List<PhoneFeatureStoreEntry> findByScoreDateOrderByReachScoreDesc(LocalDate scoreDate);
}
