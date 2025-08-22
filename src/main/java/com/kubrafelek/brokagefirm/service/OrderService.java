package com.kubrafelek.brokagefirm.service;

import com.kubrafelek.brokagefirm.entity.Order;
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

    public OrderService(OrderRepository orderRepository, AssetService assetService) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
    }

    public Order createOrder(Long userId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        BigDecimal totalAmount = size.multiply(price);

        if (side == OrderSide.BUY) {
            // For BUY orders: (order.size * order.price) <= customer's TRY asset.usableSize
            if (!assetService.hasEnoughUsableBalance(userId, TRY_ASSET, totalAmount)) {
                throw new InsufficientBalanceException(Constants.ErrorMessages.INSUFFICIENT_USABLE_BALANCE);
            }
            assetService.reserveAsset(userId, TRY_ASSET, totalAmount);
        } else {
            // For SELL orders: order.size <= customer's [assetName] asset.usableSize
            if (!assetService.hasEnoughUsableBalance(userId, assetName, size)) {
                throw new InsufficientBalanceException(Constants.ErrorMessages.INSUFFICIENT_USABLE_BALANCE);
            }
            assetService.reserveAsset(userId, assetName, size);
        }

        Order order = new Order(userId, assetName, side, size, price, OrderStatus.PENDING, LocalDateTime.now());
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByUserIdAndCreateDateBetween(userId, startDate, endDate);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getAllOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByCreateDateBetween(startDate, endDate);
        return orders;
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    public Order cancelOrder(Long orderId, Long userId, boolean isAdmin) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new OrderNotFoundException(Constants.ErrorMessages.ORDER_NOT_FOUND);
        }

        Order order = orderOpt.get();

        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException(Constants.ErrorMessages.YOU_CAN_ONLY_CANCEL_YOUR_OWN_ORDERS);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(Constants.ErrorMessages.ONLY_PENDING_ORDERS_CAN_BE_CANCELLED);
        }

        BigDecimal totalAmount = order.getSize().multiply(order.getPrice());
        if (order.getOrderSide() == OrderSide.BUY) {
            assetService.releaseAsset(order.getUserId(), TRY_ASSET, totalAmount);
        } else {
            assetService.releaseAsset(order.getUserId(), order.getAssetName(), order.getSize());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public Order matchOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new OrderNotFoundException(Constants.ErrorMessages.ORDER_NOT_FOUND);
        }

        Order order = orderOpt.get();

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(Constants.ErrorMessages.ONLY_PENDING_ORDERS_CAN_BE_MATCHED);
        }

        BigDecimal totalAmount = order.getSize().multiply(order.getPrice());

        if (order.getOrderSide() == OrderSide.BUY) {
            assetService.transferAsset(order.getUserId(), order.getUserId(), order.getAssetName(), order.getSize());
        } else {
            assetService.transferAsset(order.getUserId(), order.getUserId(), TRY_ASSET, totalAmount);
        }

        order.setStatus(OrderStatus.MATCHED);
        return orderRepository.save(order);
    }
}
