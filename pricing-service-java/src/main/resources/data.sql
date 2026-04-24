insert into product_prices (sku, store_id, base_price, currency, inventory_on_hand, shipping_eligible, pickup_eligible) values
('SKU-RED-CHAIR', 'STORE-100', 120.00, 'USD', 25, true, true),
('SKU-BLUE-LAMP', 'STORE-100', 80.00, 'USD', 6, true, false),
('SKU-GREEN-RUG', 'STORE-100', 200.00, 'USD', 8, false, true);

insert into promotions (id, sku, description, type, amount_off, percent_off, min_quantity, customer_segment, channel, coupon_code, starts_at, ends_at, priority) values
('PROMO-CHAIR-10P', 'SKU-RED-CHAIR', 'Chair markdown', 'PERCENT_OFF', null, 10.00, 1, 'ALL', 'ANY', null, timestamp '2025-01-01 00:00:00', timestamp '2030-01-01 00:00:00', 50),
('COUPON-CHAIR-15', 'SKU-RED-CHAIR', 'Loyalty coupon', 'AMOUNT_OFF', 15.00, null, 1, 'LOYALTY', 'ONLINE', 'SAVE15', timestamp '2025-01-01 00:00:00', timestamp '2030-01-01 00:00:00', 100),
('BULK-LAMP-5P', 'SKU-BLUE-LAMP', 'Lamp bulk discount', 'PERCENT_OFF', null, 5.00, 3, 'ALL', 'ANY', null, timestamp '2025-01-01 00:00:00', timestamp '2030-01-01 00:00:00', 60),
('VIP-RUG-20P', 'SKU-GREEN-RUG', 'VIP rug discount', 'PERCENT_OFF', null, 20.00, 1, 'VIP', 'ANY', null, timestamp '2025-01-01 00:00:00', timestamp '2030-01-01 00:00:00', 80);

