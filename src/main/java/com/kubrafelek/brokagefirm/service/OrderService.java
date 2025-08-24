package com.kubrafelek.brokagefirm.service;

import com.kubrafelek.brokagefirm.entity.Order;
import com.kubrafelek.brokagefirm.entity.Asset;
import com.kubrafelek.brokagefirm.repository.AssetRepository;
import com.kubrafelek.brokagefirm.enums.OrderSide;
import com.kubrafelek.brokagefirm.enums.OrderStatus;
import com.kubrafelek.brokagefirm.exception.InvalidOrderStatusException;
import com.kubrafelek.brokagefirm.exception.OrderNotFoundException;
import com.kubrafelek.brokagefirm.exception.UnauthorizedOrderAccessException;
import com.kubrafelek.brokagefirm.exception.InvalidAssetException;
import com.kubrafelek.brokagefirm.exception.InvalidOrderException;
import com.kubrafelek.brokagefirm.repository.OrderRepository;
import com.kubrafelek.brokagefirm.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String TRY_ASSET = "TRY";
    private static final BigDecimal MIN_ORDER_SIZE = BigDecimal.valueOf(0.01);
    private static final BigDecimal MIN_PRICE = BigDecimal.valueOf(0.01);

    private final OrderRepository orderRepository;
    private final AssetService assetService;
    private final AssetRepository assetRepository;

    public OrderService(OrderRepository orderRepository, AssetService assetService, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
        this.assetRepository = assetRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Order createOrder(Long userId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        logger.info("Creating order for user: {}, asset: {}, side: {}, size: {}, price: {}",
            userId, assetName, side, size, price);

        validateOrderParameters(assetName, size, price);

        BigDecimal totalAmount = size.multiply(price);
        logger.info("Calculated total amount for order: {}", totalAmount);

        try {
            if (side == OrderSide.BUY) {
                logger.info("Processing BUY order - atomic TRY reservation for user: {}, required amount: {}", userId, totalAmount);
                assetService.atomicReserveAsset(userId, TRY_ASSET, totalAmount);
                logger.info("Successfully reserved TRY asset for BUY order - user: {}, amount: {}", userId, totalAmount);
            } else {
                logger.info("Processing SELL order - atomic {} reservation for user: {}, required size: {}", assetName, userId, size);
                assetService.atomicReserveAsset(userId, assetName, size);
                logger.info("Successfully reserved {} asset for SELL order - user: {}, size: {}", assetName, userId, size);
            }

            Order order = new Order(userId, assetName, side, size, price, OrderStatus.PENDING, LocalDateTime.now());
            Order savedOrder = orderRepository.save(order);
            logger.info("Order created successfully with ID: {} for user: {}, asset: {}, side: {}",
                savedOrder.getId(), userId, assetName, side);
            return savedOrder;
        } catch (Exception e) {
            logger.error("Failed to create order for user: {}, asset: {}, error: {}", userId, assetName, e.getMessage());
            throw e;
        }
    }

    private void validateOrderParameters(String assetName, BigDecimal size, BigDecimal price) {
        if (!assetService.isValidTradableAsset(assetName)) {
            throw new InvalidAssetException("Asset " + assetName + " is not available for trading");
        }

        if (size == null || size.compareTo(MIN_ORDER_SIZE) < 0) {
            throw new InvalidOrderException("Minimum order size is " + MIN_ORDER_SIZE);
        }
        if (price == null || price.compareTo(MIN_PRICE) < 0) {
            throw new InvalidOrderException("Minimum price is " + MIN_PRICE + " TRY");
        }
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

    @Transactional(isolation = Isolation.SERIALIZABLE)
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

        try {
            performAtomicAssetTransfer(order, totalAmount);

            order.setStatus(OrderStatus.MATCHED);
            Order matchedOrder = orderRepository.save(order);
            logger.info("Order matched successfully - orderId: {}, user: {}, asset: {}, side: {}",
                orderId, order.getUserId(), order.getAssetName(), order.getOrderSide());
            return matchedOrder;
        } catch (Exception e) {
            logger.error("Failed to match order: {}, error: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to match order: " + e.getMessage(), e);
        }
    }

    private void performAtomicAssetTransfer(Order order, BigDecimal totalAmount) {
        if (order.getOrderSide() == OrderSide.BUY) {
            logger.info("Processing BUY order match - deducting TRY and adding target asset");
            deductMoneyFromUser(order, totalAmount);
            addAssetToUser(order);
        } else {
            logger.info("Processing SELL order match - deducting target asset and adding TRY");
            deductTargetAssetFromUser(order);
            addMoneyToUser(order, totalAmount);
        }
    }

    private void addMoneyToUser(Order order, BigDecimal totalAmount) {
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

    private void deductTargetAssetFromUser(Order order) {
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
    }

    private void addAssetToUser(Order order) {
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
    }

    private void deductMoneyFromUser(Order order, BigDecimal totalAmount) {
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
    }
}
