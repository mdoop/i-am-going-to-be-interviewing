package com.example.retail.kotlinapp.repository

import com.example.retail.contract.Channel
import com.example.retail.contract.CustomerSegment
import com.example.retail.contract.PromotionRule
import com.example.retail.contract.PromotionType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
open class JdbcPromotionRepository(
    private val jdbcTemplate: JdbcTemplate,
) : PromotionRepository {
    override fun findBySku(sku: String): List<PromotionRule> =
        jdbcTemplate.query(
            """
            select id, sku, description, type, amount_off, percent_off, min_quantity,
                   customer_segment, channel, coupon_code, starts_at, ends_at, priority
            from promotions
            where sku = ?
            order by priority desc, id asc
            """.trimIndent(),
            { rs, _ ->
                PromotionRule(
                    rs.getString("id"),
                    rs.getString("sku"),
                    rs.getString("description"),
                    PromotionType.valueOf(rs.getString("type")),
                    rs.getBigDecimal("amount_off"),
                    rs.getBigDecimal("percent_off"),
                    rs.getInt("min_quantity"),
                    CustomerSegment.valueOf(rs.getString("customer_segment")),
                    Channel.valueOf(rs.getString("channel")),
                    rs.getString("coupon_code"),
                    rs.getTimestamp("starts_at").toInstant(),
                    rs.getTimestamp("ends_at").toInstant(),
                    rs.getInt("priority"),
                )
            },
            sku,
        )

    override fun replaceAll(promotions: List<PromotionRule>) {
        promotions.forEach { rule ->
            jdbcTemplate.update("delete from promotions where id = ?", rule.id())
            jdbcTemplate.update(
                """
                insert into promotions
                (id, sku, description, type, amount_off, percent_off, min_quantity,
                 customer_segment, channel, coupon_code, starts_at, ends_at, priority)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                rule.id(),
                rule.sku(),
                rule.description(),
                rule.type().name,
                rule.amountOff(),
                rule.percentOff(),
                rule.minQuantity(),
                rule.customerSegment().name,
                rule.channel().name,
                rule.couponCode(),
                Timestamp.from(rule.startsAt()),
                Timestamp.from(rule.endsAt()),
                rule.priority(),
            )
        }
    }
}
