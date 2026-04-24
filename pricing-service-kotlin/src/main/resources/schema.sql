create table if not exists product_prices (
    sku varchar(64) not null,
    store_id varchar(64) not null,
    base_price numeric(10, 2) not null,
    currency varchar(8) not null,
    inventory_on_hand integer not null,
    shipping_eligible boolean not null,
    pickup_eligible boolean not null,
    primary key (sku, store_id)
);

create table if not exists promotions (
    id varchar(64) primary key,
    sku varchar(64) not null,
    description varchar(255) not null,
    type varchar(32) not null,
    amount_off numeric(10, 2),
    percent_off numeric(10, 2),
    min_quantity integer not null,
    customer_segment varchar(32) not null,
    channel varchar(32) not null,
    coupon_code varchar(64),
    starts_at timestamp not null,
    ends_at timestamp not null,
    priority integer not null
);

