package com.kubrafelek.brokagefirm.repository;

import com.kubrafelek.brokagefirm.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByUserId(Long userId);
    Optional<Asset> findByUserIdAndAssetName(Long userId, String assetName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.userId = :userId AND a.assetName = :assetName")
    Optional<Asset> findByUserIdAndAssetNameForUpdate(Long userId, String assetName);

    boolean existsByAssetName(String assetName);

    @Query("SELECT DISTINCT a.assetName FROM Asset a ORDER BY a.assetName")
    List<String> findDistinctAssetNames();
}
