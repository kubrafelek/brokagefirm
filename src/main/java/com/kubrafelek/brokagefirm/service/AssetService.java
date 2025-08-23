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

    public void transferAsset(Long fromUserId, Long toUserId, String assetName, BigDecimal amount) {
        logger.info("Attempting to transfer asset from user: {} to user: {}, asset: {}, amount: {}",
            fromUserId, toUserId, assetName, amount);

        // Transfer from source user
        Optional<Asset> fromAssetOpt = assetRepository.findByUserIdAndAssetName(fromUserId, assetName);
        if (fromAssetOpt.isPresent()) {
            Asset fromAsset = fromAssetOpt.get();
            BigDecimal newSize = fromAsset.getSize().subtract(amount);
            BigDecimal newUsableSize = fromAsset.getUsableSize().subtract(amount);
            fromAsset.setSize(newSize);
            fromAsset.setUsableSize(newUsableSize);
            assetRepository.save(fromAsset);
            logger.info("Asset deducted from source user: {}, asset: {}, amount: {}, new size: {}, new usable size: {}",
                fromUserId, assetName, amount, newSize, newUsableSize);
        } else {
            logger.warn("Source asset not found for transfer - fromUserId: {}, assetName: {}", fromUserId, assetName);
        }

        // Transfer to destination user
        Optional<Asset> toAssetOpt = assetRepository.findByUserIdAndAssetName(toUserId, assetName);
        if (toAssetOpt.isPresent()) {
            Asset toAsset = toAssetOpt.get();
            BigDecimal newSize = toAsset.getSize().add(amount);
            BigDecimal newUsableSize = toAsset.getUsableSize().add(amount);
            toAsset.setSize(newSize);
            toAsset.setUsableSize(newUsableSize);
            assetRepository.save(toAsset);
            logger.info("Asset added to destination user: {}, asset: {}, amount: {}, new size: {}, new usable size: {}",
                toUserId, assetName, amount, newSize, newUsableSize);
        } else {
            Asset newAsset = new Asset(toUserId, assetName, amount, amount);
            assetRepository.save(newAsset);
            logger.info("New asset created for destination user: {}, asset: {}, amount: {}",
                toUserId, assetName, amount);
        }

        logger.info("Asset transfer completed successfully from user: {} to user: {}, asset: {}, amount: {}",
            fromUserId, toUserId, assetName, amount);
    }
}
