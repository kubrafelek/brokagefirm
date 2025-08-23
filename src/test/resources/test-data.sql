-- Test data for integration tests
 -- Insert Users (passwords are encoded with BCrypt)
-- admin password: admin123
-- customer passwords: pass123

INSERT INTO users (username, password, role)
VALUES ('admin',
        '$2a$10$2vKSDFpNUmOQHZ4fQeEzxeWH6E4wPEA31J/oDI3Squ.6WasK.nu7W',
        'ADMIN'), ('customer1',
                   '$2a$10$LU5H9bJPDwL0kpqzivgRC.ztYr0C/FHxkI1GbBt2YUP4MrBxge412',
                   'CUSTOMER'), ('customer2',
                                 '$2a$10$LU5H9bJPDwL0kpqzivgRC.ztYr0C/FHxkI1GbBt2YUP4MrBxge412',
                                 'CUSTOMER'), ('testuser',
                                               '$2a$10$LU5H9bJPDwL0kpqzivgRC.ztYr0C/FHxkI1GbBt2YUP4MrBxge412',
                                               'CUSTOMER');

-- Insert Assets for customers
-- Customer 1 assets (ID: 2)

INSERT INTO assets (user_id, asset_name, size, usable_size)
VALUES (2,
        'TRY',
        10000.00,
        10000.00), (2,
                    'AAPL',
                    10.00,
                    10.00), (2,
                             'GOOGL',
                             5.00,
                             5.00);

-- Customer 2 assets (ID: 3)

INSERT INTO assets (user_id, asset_name, size, usable_size)
VALUES (3,
        'TRY',
        15000.00,
        15000.00), (3,
                    'MSFT',
                    20.00,
                    20.00), (3,
                             'TSLA',
                             8.00,
                             8.00);

-- Test user assets (ID: 4)

INSERT INTO assets (user_id, asset_name, size, usable_size)
VALUES (4,
        'TRY',
        5000.00,
        5000.00), (4,
                   'NVDA',
                   12.00,
                   12.00);

-- Insert sample orders for testing

INSERT INTO orders (user_id, asset_name, order_side, size, price, status, create_date)
VALUES -- Pending orders
(2,
 'AAPL',
 'BUY',
 5.00,
 150.00,
 'PENDING',
 '2025-08-23 10:00:00'), (3,
                          'GOOGL',
                          'SELL',
                          3.00,
                          2800.00,
                          'PENDING',
                          '2025-08-23 10:15:00'), (4,
                                                   'TSLA',
                                                   'BUY',
                                                   2.00,
                                                   250.00,
                                                   'PENDING',
                                                   '2025-08-23 10:30:00'), -- Matched orders
(2,
 'GOOGL',
 'BUY',
 2.00,
 2750.00,
 'MATCHED',
 '2025-08-22 09:00:00'), (3,
                          'MSFT',
                          'SELL',
                          5.00,
                          400.00,
                          'MATCHED',
                          '2025-08-22 09:15:00'), -- Cancelled orders
(2,
 'TSLA',
 'SELL',
 1.00,
 260.00,
 'CANCELLED',
 '2025-08-21 15:00:00');

