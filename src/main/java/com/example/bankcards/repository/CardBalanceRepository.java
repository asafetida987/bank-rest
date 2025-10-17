package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardBalanceRepository extends JpaRepository<CardBalance, Long> {
}
