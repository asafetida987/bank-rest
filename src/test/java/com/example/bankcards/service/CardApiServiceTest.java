package com.example.bankcards.service;

import com.example.bankcards.dto.request.card.CardRequestDTO;
import com.example.bankcards.dto.request.card.NewCardRequestDTO;
import com.example.bankcards.dto.request.card.TransferRequestDTO;
import com.example.bankcards.dto.response.MessageResponseDTO;
import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.card.BalanceResponseDTO;
import com.example.bankcards.dto.response.card.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBalance;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardApiServiceTest {

    @Mock
    private CardAdminService cardAdminService;
    @Mock
    private CardUserService cardUserService;

    @InjectMocks
    private CardApiService cardApiService;

    private User user;
    private Card card;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(11L)
                .login("test")
                .build();

        card = Card.builder()
                .id(1L)
                .owner(user)
                .cardNumber("1234")
                .cardStatus(CardStatus.ACTIVE)
                .build();
    }

    @Test
    void getMyCards_shouldReturnPagedResponseDTO() {
        int page = 0;
        int size = 10;
        Pageable expectedPageable = PageRequest.of(page, size);

        Page<Card> cards = new PageImpl<>(List.of(card), expectedPageable, 1);

        when(cardUserService.getMyCards(ArgumentMatchers.<Specification<Card>>any(), eq(expectedPageable))).thenReturn(cards);

        PagedResponseDTO<CardResponseDTO> responseDTO = cardApiService.getMyCards(user, page, size, null, null, null, null, null, null);

        assertEquals(1, responseDTO.totalElements());
        assertEquals(1L, responseDTO.content().get(0).id());
        verify(cardUserService, times(1)).getMyCards(ArgumentMatchers.<Specification<Card>>any(), any(Pageable.class));
    }

    @Test
    void getBalanceByCard_shouldReturnBalanceResponseDTO() {
        CardBalance cardBalance = CardBalance.builder()
                .card(card)
                .balance(BigDecimal.TEN)
                .build();

        when(cardUserService.getCardBalance(any(User.class), anyLong())).thenReturn(cardBalance);

        BalanceResponseDTO result = cardApiService.getBalanceByCard(user, card.getId());

        assertEquals(cardBalance.getBalance(), result.balance());
        verify(cardUserService, times(1)).getCardBalance(any(User.class), anyLong());
    }

    @Test
    void transfer_shouldReturnMessageResponseDTO() {
        TransferRequestDTO requestDTO = new TransferRequestDTO(1L, 2L, BigDecimal.TEN);

        MessageResponseDTO messageResponseDTO = cardApiService.transfer(user, requestDTO);

        assertEquals("Перевод осуществлен успешно", messageResponseDTO.message());
    }

    @Test
    void requestBlock_shouldReturnMessageResponseDTO() {
        CardRequestDTO requestDTO = new CardRequestDTO(1L);

        MessageResponseDTO messageResponseDTO = cardApiService.requestBlock(user, requestDTO);

        assertEquals("Запрос блокировки успешно отправлен", messageResponseDTO.message());
    }

    @Test
    void getAllCards_shouldReturnPagedResponseDTO() {
        int page = 0;
        int size = 10;
        Pageable expectedPageable = PageRequest.of(page, size);

        Page<Card> cards = new PageImpl<>(List.of(card), expectedPageable, 1);

        when(cardAdminService.getAll(ArgumentMatchers.<Specification<Card>>any(), eq(expectedPageable))).thenReturn(cards);

        PagedResponseDTO<CardResponseDTO> responseDTO = cardApiService.getAllCards(page, size, null, null, null, null, null, null, null);

        assertEquals(1, responseDTO.totalElements());
        assertEquals(1L, responseDTO.content().get(0).id());
        verify(cardAdminService, times(1)).getAll(ArgumentMatchers.<Specification<Card>>any(), any(Pageable.class));
    }

    @Test
    void createNewCard_shouldReturnCardResponseDTO() {
        NewCardRequestDTO requestDTO = new NewCardRequestDTO(1L);

        when(cardAdminService.create(1L)).thenReturn(card);

        CardResponseDTO responseDTO = cardApiService.createNewCard(requestDTO);

        assertNotNull(responseDTO);
    }

    @Test
    void blockCard_shouldReturnMessageResponseDTO() {
        CardRequestDTO requestDTO = new CardRequestDTO(1L);

        MessageResponseDTO messageResponseDTO = cardApiService.blockCard(requestDTO);

        assertEquals("Карта успешно заблокирована", messageResponseDTO.message());
    }

    @Test
    void activateCard_shouldReturnMessageResponseDTO() {
        CardRequestDTO requestDTO = new CardRequestDTO(1L);

        MessageResponseDTO messageResponseDTO = cardApiService.activateCard(requestDTO);

        assertEquals("Карта успешно активирована", messageResponseDTO.message());
    }

    @Test
    void deleteCard_shouldReturnMessageResponseDTO() {
        MessageResponseDTO messageResponseDTO = cardApiService.deleteCard(1L);

        assertEquals("Карта успешно удалена", messageResponseDTO.message());
    }

}
