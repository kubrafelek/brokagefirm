package com.kubrafelek.brokagefirm.service;

import com.kubrafelek.brokagefirm.entity.Asset;
import com.kubrafelek.brokagefirm.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testAsset = new Asset(1L, "TRY", new BigDecimal("1000.00"), new BigDecimal("800.00"));
    }

    @Test
    void testGetAssetsByCustomerId() {
        List<Asset> expectedAssets = Arrays.asList(testAsset);
        when(assetRepository.findByUserId(1L)).thenReturn(expectedAssets);

        List<Asset> result = assetService.getAssetsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(testAsset.getAssetName(), result.get(0).getAssetName());
        verify(assetRepository).findByUserId(1L);
    }

    @Test
    void testHasEnoughUsableBalance_Sufficient() {
        when(assetRepository.findByUserIdAndAssetName(1L, "TRY"))
            .thenReturn(Optional.of(testAsset));

        boolean result = assetService.hasEnoughUsableBalance(1L, "TRY", new BigDecimal("500.00"));

        assertTrue(result);
    }

    @Test
    void testHasEnoughUsableBalance_Insufficient() {
        when(assetRepository.findByUserIdAndAssetName(1L, "TRY"))
            .thenReturn(Optional.of(testAsset));

        boolean result = assetService.hasEnoughUsableBalance(1L, "TRY", new BigDecimal("1000.00"));

        assertFalse(result);
    }

    @Test
    void testReserveAsset_Success() {
        when(assetRepository.findByUserIdAndAssetName(1L, "TRY"))
            .thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        assetService.reserveAsset(1L, "TRY", new BigDecimal("200.00"));

        assertEquals(new BigDecimal("600.00"), testAsset.getUsableSize());
        verify(assetRepository).save(testAsset);
    }

    @Test
    void testReserveAsset_InsufficientBalance() {
        when(assetRepository.findByUserIdAndAssetName(1L, "TRY"))
            .thenReturn(Optional.of(testAsset));

        assertThrows(RuntimeException.class, () ->
            assetService.reserveAsset(1L, "TRY", new BigDecimal("900.00")));
    }

    @Test
    void testReleaseAsset_Success() {
        when(assetRepository.findByUserIdAndAssetName(1L, "TRY"))
            .thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        assetService.releaseAsset(1L, "TRY", new BigDecimal("100.00"));

        assertEquals(new BigDecimal("900.00"), testAsset.getUsableSize());
        verify(assetRepository).save(testAsset);
    }
}
