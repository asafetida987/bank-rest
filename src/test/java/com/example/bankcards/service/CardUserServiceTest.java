package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBalance;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.WrongParameterException;
import com.example.bankcards.repository.CardBalanceRepository;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardUserServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardBalanceRepository cardBalanceRepository;

    @InjectMocks
    private CardUserService cardUserService;

    private User user;
    private Card card;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(11L)
                .login("test")
                .build();

        card = Card.builder()
                .id(11L)
                .cardNumber("1234")
                .cardStatus(CardStatus.ACTIVE)
                .build();
    }

    @Test
    void getMyCards_shouldReturnPageOfCards() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Specification<Card> spec = (root, query, builder) -> null;

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        Page<Card> result = cardUserService.getMyCards(spec, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(cardPage.getTotalElements(), result.getTotalElements());
        assertEquals(cardPage.getContent().get(0).getId(), result.getContent().get(0).getId());
        assertEquals(cardPage.getContent().get(0).getCardNumber(), result.getContent().get(0).getCardNumber());
    }

    @Test
    void getCardBalance_shouldReturnCardBalance() {
        CardBalance cardBalance = CardBalance.builder()
                .card(card)
                .balance(BigDecimal.TEN)
                .build();
        card.setOwner(user);
        card.setBalance(cardBalance);
        card.setCardStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdAndOwner(anyLong(), any(User.class))).thenReturn(Optional.of(card));

        CardBalance result = cardUserService.getCardBalance(user, 1L);

        assertEquals(cardBalance.getCard().getId(), result.getCard().getId());
        assertEquals(cardBalance.getBalance(), result.getBalance());

        verify(cardRepository, times(1)).findByIdAndOwner(anyLong(), any(User.class));
    }

    @Test
    void getCardBalance_shouldThrowException_whenCardNotFound() {
        when(cardRepository.findByIdAndOwner(anyLong(), any(User.class))).thenReturn(Optional.empty());

        assertThrows(WrongParameterException.class, () -> cardUserService.getCardBalance(user, 1L));
    }

    @Test
    void getCardBalance_shouldThrowException_whenCardNotActive() {
        CardBalance cardBalance = CardBalance.builder()
                .card(card)
                .balance(BigDecimal.TEN)
                .build();
        card.setOwner(user);
        card.setBalance(cardBalance);
        card.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findByIdAndOwner(anyLong(), any(User.class))).thenReturn(Optional.of(card));

        assertThrows(WrongParameterException.class, () -> cardUserService.getCardBalance(user, 1L));
    }

    @Test
    void transferMoney_shouldTransfer() {
        CardBalance cardBalance1 = CardBalance.builder()
                .card(card)
                .balance(BigDecimal.TEN)
                .build();
        Card card2 = Card.builder()
                .id(12L)
                .cardNumber("1234")
                .cardStatus(CardStatus.ACTIVE)
                .build();
        CardBalance cardBalance2 = CardBalance.builder()
                .card(card2)
                .balance(BigDecimal.ZERO)
                .build();
        card.setOwner(user);
        card2.setOwner(user);
        card.setBalance(cardBalance1);
        card2.setBalance(cardBalance2);

        when(cardRepository.findByIdAndOwner(eq(11L), any(User.class))).thenReturn(Optional.of(card));
        when(cardRepository.findByIdAndOwner(eq(12L), any(User.class))).thenReturn(Optional.of(card2));

        cardUserService.transferMoney(user, 11L, 12L, BigDecimal.ONE);

        verify(cardRepository, times(2)).findByIdAndOwner(anyLong(), any(User.class));
        verify(cardBalanceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void transferMoney_shouldThrowException_whenBalanceLessThanAmount() {
        CardBalance cardBalance1 = CardBalance.builder()
                .card(card)
                .balance(BigDecimal.ONE)
                .build();
        Card card2 = Card.builder()
                .id(12L)
                .cardNumber("1234")
                .cardStatus(CardStatus.ACTIVE)
                .build();
        CardBalance cardBalance2 = CardBalance.builder()
                .card(card2)
                .balance(BigDecimal.ZERO)
                .build();
        card.setOwner(user);
        card2.setOwner(user);
        card.setBalance(cardBalance1);
        card2.setBalance(cardBalance2);

        when(cardRepository.findByIdAndOwner(eq(11L), any(User.class))).thenReturn(Optional.of(card));
        when(cardRepository.findByIdAndOwner(eq(12L), any(User.class))).thenReturn(Optional.of(card2));

        assertThrows(WrongParameterException.class, () -> cardUserService.transferMoney(user, 11L, 12L, BigDecimal.TEN));
    }

    @Test
    void requestBlock_shouldUpdateCard(){
        when(cardRepository.findByIdAndOwner(anyLong(), any(User.class))).thenReturn(Optional.of(card));

        cardUserService.requestBlock(user, card.getId());

        verify(cardRepository, times(1)).save(any(Card.class));
    }

}
