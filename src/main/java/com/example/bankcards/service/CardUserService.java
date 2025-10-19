package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBalance;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.WrongParameterException;
import com.example.bankcards.repository.CardBalanceRepository;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardUserService {

    private final CardRepository cardRepository;
    private final CardBalanceRepository cardBalanceRepository;

    public Page<Card> getMyCards(Specification<Card> specification, Pageable pageable) {

        return cardRepository.findAll(specification, pageable);
    }

    public CardBalance getCardBalance(User currentUser, Long cardId) {
        Card card = getCard(currentUser, cardId);
        if (card.getCardStatus() != CardStatus.ACTIVE) {
            throw new WrongParameterException("Карта недоступна для перевода");
        }

        return card.getBalance();
    }

    public void transferMoney(User currentUser, Long cardIdFrom, Long cardIdTo, BigDecimal amount){
        CardBalance cardBalanceFrom = getCardBalance(currentUser, cardIdFrom);
        CardBalance cardBalanceTo = getCardBalance(currentUser, cardIdTo);

        if (cardBalanceFrom.getBalance().compareTo(amount) < 0) {
            throw new WrongParameterException("Недостаточно средств на карте списания");
        }

        cardBalanceFrom.setBalance(cardBalanceFrom.getBalance().subtract(amount));
        cardBalanceTo.setBalance(cardBalanceTo.getBalance().add(amount));

        cardBalanceRepository.saveAll(List.of(cardBalanceFrom, cardBalanceTo));

    }

    public void requestBlock(User currentUser, Long cardId){
        Card card = getCard(currentUser, cardId);
        card.setRequestBlock(true);

        cardRepository.save(card);
    }

    private Card getCard(User currentUser, Long cardId){
        return cardRepository.findByIdAndOwner(cardId, currentUser)
                .orElseThrow(() -> new WrongParameterException("Пользователь не является владельцем карты"));
    }
}
