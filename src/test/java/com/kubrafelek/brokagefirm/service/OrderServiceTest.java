package com.kubrafelek.brokagefirm.service;

import com.kubrafelek.brokagefirm.entity.Order;
import com.kubrafelek.brokagefirm.entity.Asset;
import com.kubrafelek.brokagefirm.enums.OrderSide;
import com.kubrafelek.brokagefirm.enums.OrderStatus;
import com.kubrafelek.brokagefirm.repository.OrderRepository;
import com.kubrafelek.brokagefirm.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetService assetService;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order(1L, "AAPL", OrderSide.BUY, new BigDecimal("10.00"),
                             new BigDecimal("150.00"), OrderStatus.PENDING, LocalDateTime.now());
        testOrder.setId(1L);
    }

    @Test
    void testCreateBuyOrder_Success() {
        when(assetService.hasEnoughUsableBalance(eq(1L), eq("TRY"), any(BigDecimal.class)))
            .thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(1L, "AAPL", OrderSide.BUY,
                                               new BigDecimal("10.00"), new BigDecimal("150.00"));

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(assetService).hasEnoughUsableBalance(eq(1L), eq("TRY"), any(BigDecimal.class));
        verify(assetService).reserveAsset(eq(1L), eq("TRY"), any(BigDecimal.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCreateSellOrder_Success() {
        Order sellOrder = new Order(1L, "AAPL", OrderSide.SELL, new BigDecimal("5.00"),
                                   new BigDecimal("150.00"), OrderStatus.PENDING, LocalDateTime.now());
        when(assetService.hasEnoughUsableBalance(eq(1L), eq("AAPL"), any(BigDecimal.class)))
            .thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(sellOrder);

        Order result = orderService.createOrder(1L, "AAPL", OrderSide.SELL,
                                               new BigDecimal("5.00"), new BigDecimal("150.00"));

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(assetService).hasEnoughUsableBalance(eq(1L), eq("AAPL"), any(BigDecimal.class));
        verify(assetService).reserveAsset(eq(1L), eq("AAPL"), any(BigDecimal.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCreateBuyOrder_InsufficientBalance() {
        when(assetService.hasEnoughUsableBalance(eq(1L), eq("TRY"), any(BigDecimal.class)))
            .thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            orderService.createOrder(1L, "AAPL", OrderSide.BUY,
                                    new BigDecimal("10.00"), new BigDecimal("150.00")));
    }

    @Test
    void testGetOrdersByCustomerId() {
        List<Order> expectedOrders = Collections.singletonList(testOrder);
        when(orderRepository.findByUserId(1L)).thenReturn(expectedOrders);

        List<Order> result = orderService.getOrdersByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(testOrder.getAssetName(), result.getFirst().getAssetName());
        verify(orderRepository).findByUserId(1L);
    }

    @Test
    void testCancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(1L, 1L, false);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(assetService).releaseAsset(eq(1L), eq("TRY"), any(BigDecimal.class));
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testCancelOrder_Unauthorized() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(RuntimeException.class, () ->
            orderService.cancelOrder(1L, 2L, false));
    }

    @Test
    void testCancelOrder_NotPending() {
        testOrder.setStatus(OrderStatus.MATCHED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(RuntimeException.class, () ->
            orderService.cancelOrder(1L, 1L, false));
    }

    @Test
    void testMatchBuyOrder_Success() {
        // Mock existing TRY asset for the user
        Asset tryAsset = new Asset(1L, "TRY", new BigDecimal("2000.00"), new BigDecimal("500.00"));
        when(assetRepository.findByUserIdAndAssetName(1L, "TRY"))
            .thenReturn(Optional.of(tryAsset));

        // Mock that user doesn't have AAPL asset yet (new asset will be created)
        when(assetRepository.findByUserIdAndAssetName(1L, "AAPL"))
            .thenReturn(Optional.empty());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(assetRepository.save(any(Asset.class))).thenReturn(new Asset());

        Order result = orderService.matchOrder(1L);

        assertEquals(OrderStatus.MATCHED, result.getStatus());
        verify(assetRepository).findByUserIdAndAssetName(1L, "TRY");
        verify(assetRepository).findByUserIdAndAssetName(1L, "AAPL");
        verify(assetRepository, times(2)).save(any(Asset.class)); // Save TRY asset update and new AAPL asset
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testMatchOrder_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            orderService.matchOrder(1L));
    }
}
