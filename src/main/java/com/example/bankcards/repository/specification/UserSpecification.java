package com.example.bankcards.repository.specification;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class UserSpecification {

    public static Specification<User> hasLogin(String login) {
        return ((root, query, criteriaBuilder) -> {
            if (login == null) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("login")),
                    "%" + login.toLowerCase() + "%"
            );
        });
    }

    public static Specification<User> createdAtAfter(LocalDate from) {
        return ((root, query, criteriaBuilder) -> {
            if (from == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    from.atStartOfDay()
            );
        });
    }

    public static Specification<User> createdAtBefore(LocalDate to) {
        return ((root, query, criteriaBuilder) -> {
            if (to == null) {
                return null;
            }
            return criteriaBuilder.lessThan(
                    root.get("createdAt"),
                    to.plusDays(1).atStartOfDay()
            );
        });
    }
}

