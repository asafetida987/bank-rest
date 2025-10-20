package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBalance;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.WrongParameterException;
import com.example.bankcards.repository.CardBalanceRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardAdminService {

    private final CardRepository cardRepository;
    private final CardBalanceRepository cardBalanceRepository;
    private final UserService userService;

    public Page<Card> getAll(Specification<Card> specification, Pageable pageable) {
        log.info("Админ запрашивает список карт");
        Page<Card> cards = cardRepository.findAll(specification, pageable);
        log.info("Найдено {} карт", cards.getTotalElements());

        return cards;
    }

    public Card create(Long ownerId) {
        log.info("Создание новой карты для пользователя id={}", ownerId);
        User user = userService.findUserById(ownerId);
        Card saveCard = saveNewCard(user);
        CardBalance saveBalance = saveNewCardBalance(saveCard);
        saveCard.setBalance(saveBalance);
        log.info("Карта id={} успешно создана для пользователя id={}", saveCard.getId(), ownerId);

        return saveCard;
    }

    public void blockCard(Long cardId) {
        log.info("Блокировка карты id={}", cardId);
        setStatus(cardId, CardStatus.BLOCKED);
        log.info("Карта id={} успешно заблокирована", cardId);
    }

    public void activateCard(Long cardId) {
        log.info("Активация карты id={}", cardId);
        setStatus(cardId, CardStatus.ACTIVE);
        log.info("Карта id={} успешно активирована", cardId);
    }

    public void deleteCard(Long cardId) {
        log.info("Удаление карты id={}", cardId);
        cardBalanceRepository.deleteById(cardId);
        cardRepository.deleteById(cardId);
        log.info("Карта id={} успешно удалена", cardId);
    }

    @Transactional
    public void updateForExpiredCard(LocalDate now) {
        log.info("Обновление статуса просроченных карт на дату {}", now);
        List<Card> cards = cardRepository.findAllByExpiryDateBefore(now);
        cards.forEach(card -> card.setCardStatus(CardStatus.EXPIRED));
        cardRepository.saveAll(cards);
        log.info("Обновлено {} карт до статуса EXPIRED", cards.size());
    }

    private void setStatus(Long cardId, CardStatus cardStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new WrongParameterException("Карты с таким id не существует"));
        card.setCardStatus(cardStatus);

        cardRepository.save(card);
    }

    private Card saveNewCard(User user) {
        Card card = Card.builder()
                .owner(user)
                .expiryDate(LocalDate.now().plusYears(5))
                .cardStatus(CardStatus.ACTIVE)
                .build();
        for (int i = 0; i < 5; i++) {
            card.setCardNumber(CardUtil.generateRandomNumberCard());
            try {
                return cardRepository.save(card);
            } catch (DataIntegrityViolationException e) {
                log.warn("Попытка генерации карты с уникальным номером не удалась, повторная попытка");
            }
        }
        log.error("Не удалось создать карту для пользователя id={}", user.getId());
        throw new RuntimeException("Не удалось создать карту");
    }

    private CardBalance saveNewCardBalance(Card card) {
        CardBalance balance = CardBalance.builder()
                .card(card)
                .balance(BigDecimal.ZERO)
                .build();

        return cardBalanceRepository.save(balance);
    }
}
