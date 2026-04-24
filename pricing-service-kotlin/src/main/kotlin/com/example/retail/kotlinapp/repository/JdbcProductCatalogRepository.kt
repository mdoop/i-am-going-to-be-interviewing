package com.example.retail.kotlinapp.repository

import com.example.retail.contract.ProductPrice
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class JdbcProductCatalogRepository(
    private val jdbcTemplate: JdbcTemplate,
) : ProductCatalogRepository {
    override fun findBySkuAndStoreId(sku: String, storeId: String): ProductPrice? =
        jdbcTemplate.query(
            """
            select sku, store_id, base_price, currency, inventory_on_hand, shipping_eligible, pickup_eligible
            from product_prices
            where sku = ? and store_id = ?
            """.trimIndent(),
            { rs, _ ->
                ProductPrice(
                    rs.getString("sku"),
                    rs.getString("store_id"),
                    rs.getBigDecimal("base_price"),
                    rs.getString("currency"),
                    rs.getInt("inventory_on_hand"),
                    rs.getBoolean("shipping_eligible"),
                    rs.getBoolean("pickup_eligible"),
                )
            },
            sku,
            storeId,
        ).firstOrNull()
}
