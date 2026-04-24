package com.example.retail.javaapp.repository;

import com.example.retail.contract.ProductPrice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProductCatalogRepository implements ProductCatalogRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcProductCatalogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProductPrice> findBySkuAndStoreId(String sku, String storeId) {
        List<ProductPrice> matches = jdbcTemplate.query("""
                        select sku, store_id, base_price, currency, inventory_on_hand, shipping_eligible, pickup_eligible
                        from product_prices
                        where sku = ? and store_id = ?
                        """,
                (rs, rowNum) -> new ProductPrice(
                        rs.getString("sku"),
                        rs.getString("store_id"),
                        rs.getBigDecimal("base_price"),
                        rs.getString("currency"),
                        rs.getInt("inventory_on_hand"),
                        rs.getBoolean("shipping_eligible"),
                        rs.getBoolean("pickup_eligible")
                ),
                sku,
                storeId
        );
        return matches.stream().findFirst();
    }
}

