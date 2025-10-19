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
public class CardAdminService {

    private final CardRepository cardRepository;
    private final CardBalanceRepository cardBalanceRepository;
    private final UserService userService;

    public Page<Card> getAll(Specification<Card> specification, Pageable pageable) {

        return cardRepository.findAll(specification, pageable);
    }

    public Card create(String userLogin) {
        User user = userService.findUserByLogin(userLogin);
        Card saveCard = saveNewCard(user);
        CardBalance saveBalance = saveNewCardBalance(saveCard);
        saveCard.setBalance(saveBalance);

        return saveCard;
    }

    public void blockCard(Long cardId) {
        setStatus(cardId, CardStatus.BLOCKED);
    }

    public void activateCard(Long cardId) {
        setStatus(cardId, CardStatus.ACTIVE);
    }

    public void deleteCard(Long cardId) {
        cardBalanceRepository.deleteById(cardId);
        cardRepository.deleteById(cardId);
    }

    @Transactional
    public void updateForExpiredCard(LocalDate now) {
        List<Card> cards = cardRepository.findAllByExpiryDateBefore(now);
        cards.forEach(card -> card.setCardStatus(CardStatus.EXPIRED));

        cardRepository.saveAll(cards);
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
                continue;
            }
        }

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
