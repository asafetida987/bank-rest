package com.example.bankcards.repository.specification;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBalance;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardSpecification {

    public static Specification<Card> cardOwner(User user){
        return ((root, criteriaQuery, criteriaBuilder) -> {
            if (user == null){
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("owner"),
                    user
            );
        });
    }

    public static Specification<Card> hasUserLogin(String login) {
        return ((root, query, criteriaBuilder) -> {
            if (login == null) {
                return null;
            }
            Join<Card, User> joinUser = root.join("owner");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(joinUser.get("login")),
                    "%" + login.toLowerCase() + "%"
            );
        });
    }

    public static Specification<Card> expiryDateAfter(LocalDate from) {
        return ((root, query, criteriaBuilder) -> {
            if (from == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("expiryDate"),
                    from.atStartOfDay()
            );
        });
    }

    public static Specification<Card> expiryDateBefore(LocalDate to) {
        return ((root, query, criteriaBuilder) -> {
            if (to == null) {
                return null;
            }
            return criteriaBuilder.lessThan(
                    root.get("expiryDate"),
                    to.plusDays(1).atStartOfDay()
            );
        });
    }

    public static Specification<Card> hasStatus(CardStatus status) {
        return ((root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("cardStatus"),
                    status
            );
        });
    }

    public static Specification<Card> balanceFrom(BigDecimal from) {
        return ((root, query, criteriaBuilder) -> {
            if (from == null) {
                return null;
            }
            Join<Card, CardBalance> joinBalance = root.join("balance");
            return criteriaBuilder.greaterThan(
                    joinBalance.get("balance"),
                    from
            );
        });
    }

    public static Specification<Card> balanceTo(BigDecimal to) {
        return ((root, query, criteriaBuilder) -> {
            if (to == null) {
                return null;
            }
            Join<Card, CardBalance> joinBalance = root.join("balance");
            return criteriaBuilder.lessThan(
                    joinBalance.get("balance"),
                    to
            );
        });
    }

    public static Specification<Card> isRequestBlock(Boolean requestBlock) {
        return ((root, query, criteriaBuilder) -> {
            if (requestBlock == null) {
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("isRequestBlock"),
                    requestBlock
            );
        });
    }

}

