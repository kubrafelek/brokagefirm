package com.kubrafelek.brokagefirm.service;

import com.kubrafelek.brokagefirm.entity.Order;
import com.kubrafelek.brokagefirm.entity.Asset;
import com.kubrafelek.brokagefirm.repository.AssetRepository;
import com.kubrafelek.brokagefirm.enums.OrderSide;
import com.kubrafelek.brokagefirm.enums.OrderStatus;
import com.kubrafelek.brokagefirm.exception.InsufficientBalanceException;
import com.kubrafelek.brokagefirm.exception.InvalidOrderStatusException;
import com.kubrafelek.brokagefirm.exception.OrderNotFoundException;
import com.kubrafelek.brokagefirm.exception.UnauthorizedOrderAccessException;
import com.kubrafelek.brokagefirm.repository.OrderRepository;
import com.kubrafelek.brokagefirm.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String TRY_ASSET = "TRY";

    private final OrderRepository orderRepository;
    private final AssetService assetService;
    private final AssetRepository assetRepository;

    public OrderService(OrderRepository orderRepository, AssetService assetService, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
        this.assetRepository = assetRepository;
    }

    public Order createOrder(Long userId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        logger.info("Creating order for user: {}, asset: {}, side: {}, size: {}, price: {}",
            userId, assetName, side, size, price);

        BigDecimal totalAmount = size.multiply(price);
        logger.info("Calculated total amount for order: {}", totalAmount);

        if (side == OrderSide.BUY) {
            logger.info("Processing BUY order - checking TRY balance for user: {}, required amount: {}", userId, totalAmount);
            if (!assetService.hasEnoughUsableBalance(userId, TRY_ASSET, totalAmount)) {
                logger.warn("Insufficient TRY balance for BUY order - user: {}, required: {}", userId, totalAmount);
                throw new InsufficientBalanceException(Constants.ErrorMessages.INSUFFICIENT_USABLE_BALANCE);
            }
            assetService.reserveAsset(userId, TRY_ASSET, totalAmount);
            logger.info("Reserved TRY asset for BUY order - user: {}, amount: {}", userId, totalAmount);
        } else {
            logger.info("Processing SELL order - checking {} balance for user: {}, required size: {}", assetName, userId, size);
            if (!assetService.hasEnoughUsableBalance(userId, assetName, size)) {
                logger.warn("Insufficient {} balance for SELL order - user: {}, required: {}", assetName, userId, size);
                throw new InsufficientBalanceException(Constants.ErrorMessages.INSUFFICIENT_USABLE_BALANCE);
            }
            assetService.reserveAsset(userId, assetName, size);
            logger.info("Reserved {} asset for SELL order - user: {}, size: {}", assetName, userId, size);
        }

        Order order = new Order(userId, assetName, side, size, price, OrderStatus.PENDING, LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {} for user: {}, asset: {}, side: {}",
            savedOrder.getId(), userId, assetName, side);
        return savedOrder;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        logger.info("Retrieving orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        logger.info("Found {} orders for user: {}", orders.size(), userId);
        return orders;
    }

    public List<Order> getOrdersByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Retrieving orders for user: {} between {} and {}", userId, startDate, endDate);
        List<Order> orders = orderRepository.findByUserIdAndCreateDateBetween(userId, startDate, endDate);
        logger.info("Found {} orders for user: {} in date range", orders.size(), userId);
        return orders;
    }

    public List<Order> getAllOrders() {
        logger.info("Retrieving all orders");
        List<Order> orders = orderRepository.findAll();
        logger.info("Found {} total orders", orders.size());
        return orders;
    }

    public List<Order> getAllOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Retrieving all orders between {} and {}", startDate, endDate);
        List<Order> orders = orderRepository.findByCreateDateBetween(startDate, endDate);
        logger.info("Found {} orders in date range", orders.size());
        return orders;
    }

    public List<Order> getPendingOrders() {
        logger.info("Retrieving all pending orders");
        List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);
        logger.info("Found {} pending orders", orders.size());
        return orders;
    }

    public Order cancelOrder(Long orderId, Long userId, boolean isAdmin) {
        logger.info("Attempting to cancel order: {} by user: {}, isAdmin: {}", orderId, userId, isAdmin);

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.error("Order not found for cancellation - orderId: {}", orderId);
            throw new OrderNotFoundException(Constants.ErrorMessages.ORDER_NOT_FOUND);
        }

        Order order = orderOpt.get();
        logger.info("Found order: {} for user: {}, status: {}, side: {}",
            orderId, order.getUserId(), order.getStatus(), order.getOrderSide());

        if (!isAdmin && !order.getUserId().equals(userId)) {
            logger.warn("Unauthorized cancellation attempt - order: {} belongs to user: {} but requested by user: {}",
                orderId, order.getUserId(), userId);
            throw new UnauthorizedOrderAccessException(Constants.ErrorMessages.YOU_CAN_ONLY_CANCEL_YOUR_OWN_ORDERS);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            logger.warn("Invalid order status for cancellation - order: {}, current status: {}", orderId, order.getStatus());
            throw new InvalidOrderStatusException(Constants.ErrorMessages.ONLY_PENDING_ORDERS_CAN_BE_CANCELLED);
        }

        BigDecimal totalAmount = order.getSize().multiply(order.getPrice());
        if (order.getOrderSide() == OrderSide.BUY) {
            logger.info("Releasing TRY asset for cancelled BUY order - user: {}, amount: {}", order.getUserId(), totalAmount);
            assetService.releaseAsset(order.getUserId(), TRY_ASSET, totalAmount);
        } else {
            logger.info("Releasing {} asset for cancelled SELL order - user: {}, size: {}",
                order.getAssetName(), order.getUserId(), order.getSize());
            assetService.releaseAsset(order.getUserId(), order.getAssetName(), order.getSize());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        logger.info("Order cancelled successfully - orderId: {}, user: {}", orderId, order.getUserId());
        return cancelledOrder;
    }

    public Order matchOrder(Long orderId) {
        logger.info("Attempting to match order: {}", orderId);

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.error("Order not found for matching - orderId: {}", orderId);
            throw new OrderNotFoundException(Constants.ErrorMessages.ORDER_NOT_FOUND);
        }

        Order order = orderOpt.get();
        logger.info("Found order for matching: {}, user: {}, asset: {}, side: {}, size: {}, price: {}",
            orderId, order.getUserId(), order.getAssetName(), order.getOrderSide(), order.getSize(), order.getPrice());

        if (order.getStatus() != OrderStatus.PENDING) {
            logger.warn("Invalid order status for matching - order: {}, current status: {}", orderId, order.getStatus());
            throw new InvalidOrderStatusException(Constants.ErrorMessages.ONLY_PENDING_ORDERS_CAN_BE_MATCHED);
        }

        BigDecimal totalAmount = order.getSize().multiply(order.getPrice());
        logger.info("Processing order match with total amount: {}", totalAmount);

        if (order.getOrderSide() == OrderSide.BUY) {
            logger.info("Processing BUY order match - deducting TRY and adding target asset");

            // Deduct TRY from user
            Optional<Asset> tryAssetOpt = assetRepository.findByUserIdAndAssetName(order.getUserId(), TRY_ASSET);
            if (tryAssetOpt.isPresent()) {
                Asset tryAsset = tryAssetOpt.get();
                BigDecimal newTrySize = tryAsset.getSize().subtract(totalAmount);
                tryAsset.setSize(newTrySize);
                assetRepository.save(tryAsset);
                logger.info("Deducted TRY from user: {}, amount: {}, new size: {}",
                    order.getUserId(), totalAmount, newTrySize);
            } else {
                logger.warn("TRY asset not found for user: {} during BUY order match", order.getUserId());
            }

            // Add target asset to user
            Optional<Asset> targetAssetOpt = assetRepository.findByUserIdAndAssetName(order.getUserId(), order.getAssetName());
            if (targetAssetOpt.isPresent()) {
                Asset targetAsset = targetAssetOpt.get();
                BigDecimal newSize = targetAsset.getSize().add(order.getSize());
                BigDecimal newUsableSize = targetAsset.getUsableSize().add(order.getSize());
                targetAsset.setSize(newSize);
                targetAsset.setUsableSize(newUsableSize);
                assetRepository.save(targetAsset);
                logger.info("Added {} to existing asset for user: {}, amount: {}, new size: {}, new usable size: {}",
                    order.getAssetName(), order.getUserId(), order.getSize(), newSize, newUsableSize);
            } else {
                Asset newAsset = new Asset(order.getUserId(), order.getAssetName(), order.getSize(), order.getSize());
                assetRepository.save(newAsset);
                logger.info("Created new {} asset for user: {}, size: {}",
                    order.getAssetName(), order.getUserId(), order.getSize());
            }
        } else {
            logger.info("Processing SELL order match - deducting target asset and adding TRY");

            // Deduct target asset from user
            Optional<Asset> assetOpt = assetRepository.findByUserIdAndAssetName(order.getUserId(), order.getAssetName());
            if (assetOpt.isPresent()) {
                Asset asset = assetOpt.get();
                BigDecimal newSize = asset.getSize().subtract(order.getSize());
                asset.setSize(newSize);
                assetRepository.save(asset);
                logger.info("Deducted {} from user: {}, amount: {}, new size: {}",
                    order.getAssetName(), order.getUserId(), order.getSize(), newSize);
            } else {
                logger.warn("{} asset not found for user: {} during SELL order match",
                    order.getAssetName(), order.getUserId());
            }

            // Add TRY to user
            Optional<Asset> tryAssetOpt = assetRepository.findByUserIdAndAssetName(order.getUserId(), TRY_ASSET);
            if (tryAssetOpt.isPresent()) {
                Asset tryAsset = tryAssetOpt.get();
                BigDecimal newSize = tryAsset.getSize().add(totalAmount);
                BigDecimal newUsableSize = tryAsset.getUsableSize().add(totalAmount);
                tryAsset.setSize(newSize);
                tryAsset.setUsableSize(newUsableSize);
                assetRepository.save(tryAsset);
                logger.info("Added TRY to existing asset for user: {}, amount: {}, new size: {}, new usable size: {}",
                    order.getUserId(), totalAmount, newSize, newUsableSize);
            } else {
                Asset newTryAsset = new Asset(order.getUserId(), TRY_ASSET, totalAmount, totalAmount);
                assetRepository.save(newTryAsset);
                logger.info("Created new TRY asset for user: {}, amount: {}", order.getUserId(), totalAmount);
            }
        }

        order.setStatus(OrderStatus.MATCHED);
        Order matchedOrder = orderRepository.save(order);
        logger.info("Order matched successfully - orderId: {}, user: {}, asset: {}, side: {}",
            orderId, order.getUserId(), order.getAssetName(), order.getOrderSide());
        return matchedOrder;
    }
}
