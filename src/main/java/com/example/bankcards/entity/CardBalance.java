package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "card_balances")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardBalance {

    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Card card;
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CardBalance that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CardBalance{" +
                "id=" + id +
                '}';
    }
}
