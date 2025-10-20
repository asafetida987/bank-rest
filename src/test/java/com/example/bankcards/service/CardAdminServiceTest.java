package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBalance;
import com.example.bankcards.entity.User;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardAdminServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardBalanceRepository cardBalanceRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private CardAdminService cardAdminService;

    private Card card;

    @BeforeEach
    public void setUp() {
        card = Card.builder()
                .id(11L)
                .cardNumber("1234")
                .build();
    }

    @Test
    void getAll_shouldReturnPageOfCards() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Specification<Card> spec = (root, query, builder) -> null;

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);

        Page<Card> result = cardAdminService.getAll(spec, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(cardPage.getTotalElements(), result.getTotalElements());
        assertEquals(cardPage.getContent().get(0).getId(), result.getContent().get(0).getId());
        assertEquals(cardPage.getContent().get(0).getCardNumber(), result.getContent().get(0).getCardNumber());
    }

    @Test
    void create_shouldReturnCard() {
        User user = User.builder()
                .id(2L)
                .login("test")
                .build();
        card.setOwner(user);
        CardBalance cardBalance = CardBalance.builder()
                .card(card)
                .balance(BigDecimal.TEN)
                .build();

        when(userService.findUserById(anyLong())).thenReturn(user);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardBalanceRepository.save(any(CardBalance.class))).thenReturn(cardBalance);

        Card result = cardAdminService.create(anyLong());

        assertNotNull(result);
        assertEquals(card.getId(), result.getId());
        assertEquals(card.getCardNumber(), result.getCardNumber());
        assertEquals(card.getOwner().getLogin(), result.getOwner().getLogin());
        assertEquals(card.getBalance().getBalance(), result.getBalance().getBalance());
    }

    @Test
    void blockCard_shouldUpdateCard() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

        cardAdminService.blockCard(anyLong());

        verify(cardRepository, times(1)).findById(anyLong());
    }

    @Test
    void blockCard_shouldThrowException_whenCardNotFound() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(WrongParameterException.class, () -> cardAdminService.blockCard(anyLong()));
    }

    @Test
    void activateCard_shouldUpdateCard() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

        cardAdminService.blockCard(anyLong());

        verify(cardRepository, times(1)).findById(anyLong());
    }

    @Test
    void activateCard_shouldThrowException_whenCardNotFound() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(WrongParameterException.class, () -> cardAdminService.blockCard(anyLong()));
    }

    @Test
    void deleteCard_shouldDeleteCard() {
        cardAdminService.deleteCard(anyLong());

        verify(cardBalanceRepository, times(1)).deleteById(anyLong());
        verify(cardRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void updateForExpiredCard_shouldUpdateCard() {
        when(cardRepository.findAllByExpiryDateBefore(any(LocalDate.class))).thenReturn(List.of(card));

        cardAdminService.updateForExpiredCard(LocalDate.now());

        verify(cardRepository, times(1)).findAllByExpiryDateBefore(any(LocalDate.class));
        verify(cardRepository, times(1)).saveAll(anyList());
    }


}
