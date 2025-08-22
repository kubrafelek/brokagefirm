-- Sample data for brokerage firm application

-- Insert Users (passwords are encoded with BCrypt)
-- admin password: admin123
-- customer passwords: pass123
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN'),
('customer1', '$2a$10$gSAhZrxMllrbgeg/Qkra6uQqHqmVVWa4qTBg1J8QeWMXPV.dDOGK6', 'CUSTOMER'),
('customer2', '$2a$10$gSAhZrxMllrbgeg/Qkra6uQqHqmVVWa4qTBg1J8QeWMXPV.dDOGK6', 'CUSTOMER'),
('customer3', '$2a$10$gSAhZrxMllrbgeg/Qkra6uQqHqmVVWa4qTBg1J8QeWMXPV.dDOGK6', 'CUSTOMER');

-- Insert Assets for customers
-- Customer 1 assets
INSERT INTO assets (user_id, asset_name, size, usable_size) VALUES
(2, 'TRY', 50000.00, 50000.00),
(2, 'AAPL', 100.00, 100.00),
(2, 'GOOGL', 50.00, 50.00),
(2, 'TSLA', 25.00, 25.00);

-- Customer 2 assets
INSERT INTO assets (user_id, asset_name, size, usable_size) VALUES
(3, 'TRY', 75000.00, 75000.00),
(3, 'MSFT', 80.00, 80.00),
(3, 'AMZN', 30.00, 30.00),
(3, 'NVDA', 40.00, 40.00);

-- Customer 3 assets
INSERT INTO assets (user_id, asset_name, size, usable_size) VALUES
(4, 'TRY', 100000.00, 100000.00),
(4, 'META', 60.00, 60.00),
(4, 'NFLX', 20.00, 20.00),
(4, 'AAPL', 75.00, 75.00);

-- Insert sample orders
INSERT INTO orders (user_id, asset_name, order_side, size, price, status, create_date) VALUES
-- Pending buy orders
(2, 'AAPL', 'BUY', 10.00, 150.50, 'PENDING', '2025-08-22 10:00:00'),
(3, 'GOOGL', 'BUY', 5.00, 2800.75, 'PENDING', '2025-08-22 10:15:00'),
(4, 'TSLA', 'BUY', 15.00, 250.25, 'PENDING', '2025-08-22 10:30:00'),

-- Pending sell orders
(2, 'AAPL', 'SELL', 20.00, 155.00, 'PENDING', '2025-08-22 11:00:00'),
(3, 'MSFT', 'SELL', 25.00, 420.50, 'PENDING', '2025-08-22 11:15:00'),
(4, 'META', 'SELL', 30.00, 520.75, 'PENDING', '2025-08-22 11:30:00'),

-- Matched orders
(2, 'GOOGL', 'BUY', 8.00, 2750.00, 'MATCHED', '2025-08-22 09:00:00'),
(3, 'AMZN', 'SELL', 12.00, 3400.25, 'MATCHED', '2025-08-22 09:15:00'),
(4, 'NVDA', 'BUY', 18.00, 850.50, 'MATCHED', '2025-08-22 09:30:00'),

-- Cancelled orders
(2, 'TSLA', 'SELL', 10.00, 260.00, 'CANCELLED', '2025-08-21 15:00:00'),
(3, 'AAPL', 'BUY', 12.00, 148.75, 'CANCELLED', '2025-08-21 15:30:00'),
(4, 'MSFT', 'SELL', 22.00, 425.00, 'CANCELLED', '2025-08-21 16:00:00');
