package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBalance;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.WrongParameterException;
import com.example.bankcards.repository.CardBalanceRepository;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardUserService {

    private final CardRepository cardRepository;
    private final CardBalanceRepository cardBalanceRepository;

    public Page<Card> getMyCards(Specification<Card> specification, Pageable pageable) {
        log.info("Запрос карт пользователя (фильтр, пагинация: page={}, size={})", pageable.getPageNumber(), pageable.getPageSize());
        Page<Card> cards = cardRepository.findAll(specification, pageable);
        log.info("Найдено {} карт", cards.getTotalElements());

        return cards;
    }

    public CardBalance getCardBalance(User currentUser, Long cardId) {
        log.info("Запрос баланса карты id={} для пользователя id={}", cardId, currentUser.getId());
        Card card = getCard(currentUser, cardId);
        if (card.getCardStatus() != CardStatus.ACTIVE) {
            log.warn("Карта id={} не активна", cardId);
            throw new WrongParameterException("Карта не активна");
        }
        log.info("Баланс карты id={} успешно получен", cardId);

        return card.getBalance();
    }

    public void transferMoney(User currentUser, Long cardIdFrom, Long cardIdTo, BigDecimal amount) {
        log.info("Пользователь id={} переводит средства", currentUser.getId());
        CardBalance cardBalanceFrom = getCardBalance(currentUser, cardIdFrom);
        CardBalance cardBalanceTo = getCardBalance(currentUser, cardIdTo);

        if (cardBalanceFrom.getBalance().compareTo(amount) < 0) {
            log.warn("Недостаточно средств на карте id={} для перевода", cardIdFrom);
            throw new WrongParameterException("Недостаточно средств на карте списания");
        }

        cardBalanceFrom.setBalance(cardBalanceFrom.getBalance().subtract(amount));
        cardBalanceTo.setBalance(cardBalanceTo.getBalance().add(amount));

        cardBalanceRepository.saveAll(List.of(cardBalanceFrom, cardBalanceTo));
        log.info("Перевод средств с карты id={} на карту id={} выполнен успешно", cardIdFrom, cardIdTo);
    }

    public void requestBlock(User currentUser, Long cardId) {
        log.info("Пользователь id={} отправляет запрос на блокировку карты id={}", currentUser.getId(), cardId);
        Card card = getCard(currentUser, cardId);
        card.setRequestBlock(true);
        log.info("Запрос на блокировку карты id={} успешно отправлен", cardId);

        cardRepository.save(card);
    }

    private Card getCard(User currentUser, Long cardId) {
        return cardRepository.findByIdAndOwner(cardId, currentUser)
                .orElseThrow(() -> {
                    log.warn("Пользователь id={} не является владельцем карты id={}", currentUser.getId(), cardId);
                    return new WrongParameterException("Пользователь не является владельцем карты");
                });
    }
}
