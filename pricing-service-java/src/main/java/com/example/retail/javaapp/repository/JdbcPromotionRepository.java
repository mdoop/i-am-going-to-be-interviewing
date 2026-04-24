package com.example.retail.javaapp.repository;

import com.example.retail.contract.Channel;
import com.example.retail.contract.CustomerSegment;
import com.example.retail.contract.PromotionRule;
import com.example.retail.contract.PromotionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class JdbcPromotionRepository implements PromotionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPromotionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PromotionRule> findBySku(String sku) {
        return jdbcTemplate.query("""
                        select id, sku, description, type, amount_off, percent_off, min_quantity,
                               customer_segment, channel, coupon_code, starts_at, ends_at, priority
                        from promotions
                        where sku = ?
                        order by priority desc, id asc
                        """,
                (rs, rowNum) -> new PromotionRule(
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
                        rs.getInt("priority")
                ),
                sku
        );
    }

    @Override
    public void replaceAll(List<PromotionRule> promotions) {
        promotions.forEach(rule -> {
            jdbcTemplate.update("delete from promotions where id = ?", rule.id());
            jdbcTemplate.update("""
                            insert into promotions
                            (id, sku, description, type, amount_off, percent_off, min_quantity,
                             customer_segment, channel, coupon_code, starts_at, ends_at, priority)
                            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    rule.id(),
                    rule.sku(),
                    rule.description(),
                    rule.type().name(),
                    rule.amountOff(),
                    rule.percentOff(),
                    rule.minQuantity(),
                    rule.customerSegment().name(),
                    rule.channel().name(),
                    rule.couponCode(),
                    Timestamp.from(rule.startsAt()),
                    Timestamp.from(rule.endsAt()),
                    rule.priority()
            );
        });
    }
}

