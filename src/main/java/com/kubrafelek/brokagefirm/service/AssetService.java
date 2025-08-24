package com.kubrafelek.brokagefirm.service;

import com.kubrafelek.brokagefirm.entity.Asset;
import com.kubrafelek.brokagefirm.exception.InsufficientBalanceException;
import com.kubrafelek.brokagefirm.exception.AssetNotFoundException;
import com.kubrafelek.brokagefirm.repository.AssetRepository;
import com.kubrafelek.brokagefirm.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);
    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> getAssetsByUserId(Long userId) {
        logger.info("Retrieving assets for user: {}", userId);
        List<Asset> assets = assetRepository.findByUserId(userId);
        logger.info("Found {} assets for user: {}", assets.size(), userId);
        return assets;
    }

    public boolean hasEnoughUsableBalance(Long userId, String assetName, BigDecimal requiredAmount) {
        logger.info("Checking usable balance for user: {}, asset: {}, required amount: {}", userId, assetName, requiredAmount);
        Optional<Asset> assetOpt = assetRepository.findByUserIdAndAssetName(userId, assetName);

        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            boolean hasEnough = asset.getUsableSize().compareTo(requiredAmount) >= 0;
            logger.info("User {} has {} usable balance for asset {}: current={}, required={}",
                userId, hasEnough ? "sufficient" : "insufficient", assetName, asset.getUsableSize(), requiredAmount);
            return hasEnough;
        } else {
            logger.warn("Asset not found for user: {}, assetName: {}", userId, assetName);
            return false;
        }
    }

    public void reserveAsset(Long userId, String assetName, BigDecimal amount) {
        logger.info("Attempting to reserve asset for user: {}, asset: {}, amount: {}", userId, assetName, amount);
        Optional<Asset> assetOpt = assetRepository.findByUserIdAndAssetName(userId, assetName);
        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            BigDecimal currentUsableSize = asset.getUsableSize();
            BigDecimal newUsableSize = currentUsableSize.subtract(amount);

            if (newUsableSize.compareTo(BigDecimal.ZERO) < 0) {
                logger.warn("Insufficient usable balance for reservation - user: {}, asset: {}, current: {}, requested: {}",
                    userId, assetName, currentUsableSize, amount);
                throw new InsufficientBalanceException(Constants.ErrorMessages.INSUFFICIENT_USABLE_BALANCE);
            }

            asset.setUsableSize(newUsableSize);
            assetRepository.save(asset);
            logger.info("Asset reserved successfully for user: {}, asset: {}, amount: {}, new usable size: {}",
                userId, assetName, amount, newUsableSize);
        } else {
            logger.error("Asset not found for reservation - user: {}, assetName: {}", userId, assetName);
            throw new AssetNotFoundException(Constants.ErrorMessages.ASSET_NOT_FOUND);
        }
    }

    public void releaseAsset(Long userId, String assetName, BigDecimal amount) {
        logger.info("Attempting to release asset for user: {}, asset: {}, amount: {}", userId, assetName, amount);
        Optional<Asset> assetOpt = assetRepository.findByUserIdAndAssetName(userId, assetName);
        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            BigDecimal currentUsableSize = asset.getUsableSize();
            BigDecimal newUsableSize = currentUsableSize.add(amount);
            asset.setUsableSize(newUsableSize);
            assetRepository.save(asset);
            logger.info("Asset released successfully for user: {}, asset: {}, amount: {}, new usable size: {}",
                userId, assetName, amount, newUsableSize);
        } else {
            logger.warn("Attempted to release asset that doesn't exist: userId={}, assetName={}", userId, assetName);
        }
    }

    public List<String> getAvailableAssetNames() {
        logger.info("Retrieving available asset names for trading");
        List<String> assetNames = assetRepository.findDistinctAssetNames();
        // Filter out TRY as it's not a tradeable asset
        List<String> tradeableAssets = assetNames.stream()
            .filter(name -> !"TRY".equals(name))
            .sorted()
            .toList();
        logger.info("Found {} tradeable assets: {}", tradeableAssets.size(), tradeableAssets);
        return tradeableAssets;
    }

    public boolean isValidTradableAsset(String assetName) {
        if (assetName == null || assetName.trim().isEmpty()) {
            return false;
        }

        // TRY is not a tradeable asset - it's the base currency
        if ("TRY".equals(assetName)) {
            return false;
        }

        // Check if asset exists in the system
        return assetRepository.existsByAssetName(assetName);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void atomicReserveAsset(Long userId, String assetName, BigDecimal amount) {
        logger.info("Attempting atomic reservation for user: {}, asset: {}, amount: {}", userId, assetName, amount);

        // Use pessimistic locking to prevent race conditions
        Asset asset = assetRepository.findByUserIdAndAssetNameForUpdate(userId, assetName)
            .orElseThrow(() -> new AssetNotFoundException(Constants.ErrorMessages.ASSET_NOT_FOUND));

        BigDecimal newUsableSize = asset.getUsableSize().subtract(amount);
        if (newUsableSize.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Insufficient usable balance for atomic reservation - user: {}, asset: {}, current: {}, requested: {}",
                userId, assetName, asset.getUsableSize(), amount);
            throw new InsufficientBalanceException(Constants.ErrorMessages.INSUFFICIENT_USABLE_BALANCE);
        }

        asset.setUsableSize(newUsableSize);
        assetRepository.save(asset);
        logger.info("Atomic asset reservation successful for user: {}, asset: {}, amount: {}, new usable size: {}",
            userId, assetName, amount, newUsableSize);
    }
}
