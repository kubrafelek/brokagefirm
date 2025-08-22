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
        return assetRepository.findByUserId(userId);
    }

    public boolean hasEnoughUsableBalance(Long userId, String assetName, BigDecimal requiredAmount) {
        Optional<Asset> assetOpt = assetRepository.findByUserIdAndAssetName(userId, assetName);
        return assetOpt.filter(asset -> asset.getUsableSize().compareTo(requiredAmount) >= 0).isPresent();
    }

    public void reserveAsset(Long userId, String assetName, BigDecimal amount) {
        Optional<Asset> assetOpt = assetRepository.findByUserIdAndAssetName(userId, assetName);
        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            BigDecimal newUsableSize = asset.getUsableSize().subtract(amount);
            if (newUsableSize.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException(Constants.ErrorMessages.INSUFFICIENT_USABLE_BALANCE);
            }
            asset.setUsableSize(newUsableSize);
            assetRepository.save(asset);
        } else {
            throw new AssetNotFoundException(Constants.ErrorMessages.ASSET_NOT_FOUND);
        }
    }

    public void releaseAsset(Long userId, String assetName, BigDecimal amount) {
        Optional<Asset> assetOpt = assetRepository.findByUserIdAndAssetName(userId, assetName);
        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            BigDecimal newUsableSize = asset.getUsableSize().add(amount);
            asset.setUsableSize(newUsableSize);
            assetRepository.save(asset);
        } else {
            logger.warn("Attempted to release asset that doesn't exist: userId={}, assetName={}", userId, assetName);
        }
    }

    public void transferAsset(Long fromUserId, Long toUserId, String assetName, BigDecimal amount) {
        Optional<Asset> fromAssetOpt = assetRepository.findByUserIdAndAssetName(fromUserId, assetName);
        if (fromAssetOpt.isPresent()) {
            Asset fromAsset = fromAssetOpt.get();
            BigDecimal newSize = fromAsset.getSize().subtract(amount);
            BigDecimal newUsableSize = fromAsset.getUsableSize().subtract(amount);
            fromAsset.setSize(newSize);
            fromAsset.setUsableSize(newUsableSize);
            assetRepository.save(fromAsset);
        }

        Optional<Asset> toAssetOpt = assetRepository.findByUserIdAndAssetName(toUserId, assetName);
        if (toAssetOpt.isPresent()) {
            Asset toAsset = toAssetOpt.get();
            BigDecimal newSize = toAsset.getSize().add(amount);
            BigDecimal newUsableSize = toAsset.getUsableSize().add(amount);
            toAsset.setSize(newSize);
            toAsset.setUsableSize(newUsableSize);
            assetRepository.save(toAsset);
        } else {
            Asset newAsset = new Asset(toUserId, assetName, amount, amount);
            assetRepository.save(newAsset);
        }
    }
}
