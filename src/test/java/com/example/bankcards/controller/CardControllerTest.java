package com.example.bankcards.controller;

import com.example.bankcards.dto.request.card.CardRequestDTO;
import com.example.bankcards.dto.request.card.NewCardRequestDTO;
import com.example.bankcards.dto.request.card.TransferRequestDTO;
import com.example.bankcards.dto.response.MessageResponseDTO;
import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.card.BalanceResponseDTO;
import com.example.bankcards.dto.response.card.CardResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.UserRole;
import com.example.bankcards.security.CurrentUserProvider;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.CardApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@AutoConfigureMockMvc(addFilters = false)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CardApiService cardApiService;
    @MockitoBean
    private CurrentUserProvider currentUserProvider;
    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(11L)
                .login("test")
                .role(UserRole.ROLE_USER)
                .build();
        when(currentUserProvider.get()).thenReturn(user);
    }

    @Test
    void getMyCards_shouldReturn200() throws Exception {
        CardResponseDTO responseDTO = new CardResponseDTO(1L, "number", "owner", LocalDate.now(), CardStatus.ACTIVE);
        PagedResponseDTO<CardResponseDTO> cards = new PagedResponseDTO<>(List.of(responseDTO), 0, 10, 1L);

        when(cardApiService.getMyCards(any(User.class), anyInt(), anyInt(), nullable(LocalDate.class), nullable(LocalDate.class), nullable(CardStatus.class), nullable(BigDecimal.class), nullable(BigDecimal.class), nullable(Boolean.class)))
                .thenReturn(cards);

        mockMvc.perform(get("/api/v1/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getMyCards_shouldReturn400_whenWrongRequest() throws Exception {

        mockMvc.perform(get("/api/v1/cards")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyCards_shouldReturn500_whenException() throws Exception {
        when(cardApiService.getMyCards(any(User.class), anyInt(), anyInt(), nullable(LocalDate.class), nullable(LocalDate.class), nullable(CardStatus.class), nullable(BigDecimal.class), nullable(BigDecimal.class), nullable(Boolean.class)))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBalance_shouldReturn200() throws Exception {
        BalanceResponseDTO responseDTO = new BalanceResponseDTO(BigDecimal.TEN);

        when(cardApiService.getBalanceByCard(any(User.class), anyLong())).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/cards/{cardId}/balance", 11L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(BigDecimal.TEN));
    }

    @Test
    void getBalance_shouldReturn400_whenWrongRequest() throws Exception {
        mockMvc.perform(get("/api/v1/cards/{cardId}/balance", -11L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBalance_shouldReturn500_whenException() throws Exception {
        when(cardApiService.getBalanceByCard(any(User.class), anyLong())).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/cards/{cardId}/balance", 11L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void transfer_shouldReturn200() throws Exception {
        TransferRequestDTO requestDTO = new TransferRequestDTO(1L, 2L, BigDecimal.TEN);
        MessageResponseDTO responseDTO = new MessageResponseDTO("test");

        when(cardApiService.transfer(any(User.class), any(TransferRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("test"));
    }

    @Test
    void transfer_shouldReturn400_whenWrongRequest() throws Exception {
        TransferRequestDTO requestDTO = new TransferRequestDTO(1L, -2L, BigDecimal.TEN);
        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_shouldReturn500_whenException() throws Exception {
        TransferRequestDTO requestDTO = new TransferRequestDTO(1L, 2L, BigDecimal.TEN);
        when(cardApiService.transfer(any(User.class), any(TransferRequestDTO.class))).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void requestBlock_shouldReturn200() throws Exception {
        CardRequestDTO cardRequestDTO = new CardRequestDTO(11L);
        MessageResponseDTO responseDTO = new MessageResponseDTO("test");

        when(cardApiService.requestBlock(any(User.class), any(CardRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/cards/request-block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("test"));
    }

    @Test
    void requestBlock_shouldReturn400_whenWrongRequest() throws Exception {
        CardRequestDTO cardRequestDTO = new CardRequestDTO(-11L);

        mockMvc.perform(post("/api/v1/cards/request-block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestBlock_shouldReturn500_whenException() throws Exception {
        when(cardApiService.requestBlock(any(User.class), any(CardRequestDTO.class))).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/v1/cards/request-block"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllCards_shouldReturn200() throws Exception {
        CardResponseDTO responseDTO = new CardResponseDTO(1L, "number", "owner", LocalDate.now(), CardStatus.ACTIVE);
        PagedResponseDTO<CardResponseDTO> cards = new PagedResponseDTO<>(List.of(responseDTO), 0, 10, 1L);

        when(cardApiService.getAllCards(anyInt(), anyInt(), nullable(String.class), nullable(LocalDate.class), nullable(LocalDate.class), nullable(CardStatus.class), nullable(BigDecimal.class), nullable(BigDecimal.class), nullable(Boolean.class)))
                .thenReturn(cards);

        mockMvc.perform(get("/api/v1/cards/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getAllCards_shouldReturn400_whenWrongRequest() throws Exception {

        mockMvc.perform(get("/api/v1/cards/all")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCards_shouldReturn500_whenException() throws Exception {
        when(cardApiService.getAllCards(anyInt(), anyInt(), nullable(String.class), nullable(LocalDate.class), nullable(LocalDate.class), nullable(CardStatus.class), nullable(BigDecimal.class), nullable(BigDecimal.class), nullable(Boolean.class)))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/cards/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createNewCard_shouldReturn200() throws Exception {
        NewCardRequestDTO requestDTO = new NewCardRequestDTO(1L);
        CardResponseDTO responseDTO = new CardResponseDTO(1L, "number", "owner", LocalDate.now(), CardStatus.ACTIVE);

        when(cardApiService.createNewCard(any(NewCardRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("number"));
    }

    @Test
    void createNewCard_shouldReturn400_whenWrongRequest() throws Exception {
        CardRequestDTO cardRequestDTO = new CardRequestDTO(-11L);

        mockMvc.perform(post("/api/v1/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNewCard_shouldReturn500_whenException() throws Exception {
        when(cardApiService.createNewCard(any(NewCardRequestDTO.class))).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/cards/create"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void blockCard_shouldReturn200() throws Exception {
        CardRequestDTO cardRequestDTO = new CardRequestDTO(11L);
        MessageResponseDTO responseDTO = new MessageResponseDTO("test");

        when(cardApiService.blockCard(any(CardRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/cards/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("test"));
    }

    @Test
    void blockCard_shouldReturn400_whenWrongRequest() throws Exception {
        CardRequestDTO cardRequestDTO = new CardRequestDTO(-11L);

        mockMvc.perform(patch("/api/v1/cards/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blockCard_shouldReturn500_whenException() throws Exception {
        when(cardApiService.blockCard(any(CardRequestDTO.class))).thenThrow(new RuntimeException());

        mockMvc.perform(patch("/api/v1/cards/block"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void activateCard_shouldReturn200() throws Exception {
        CardRequestDTO cardRequestDTO = new CardRequestDTO(11L);
        MessageResponseDTO responseDTO = new MessageResponseDTO("test");

        when(cardApiService.activateCard(any(CardRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/cards/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("test"));
    }

    @Test
    void activateCard_shouldReturn400_whenWrongRequest() throws Exception {
        CardRequestDTO cardRequestDTO = new CardRequestDTO(-11L);

        mockMvc.perform(patch("/api/v1/cards/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activateCard_shouldReturn500_whenException() throws Exception {
        when(cardApiService.activateCard(any(CardRequestDTO.class))).thenThrow(new RuntimeException());

        mockMvc.perform(patch("/api/v1/cards/activate"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteCard_shouldReturn200() throws Exception {
        MessageResponseDTO responseDTO = new MessageResponseDTO("test");

        when(cardApiService.deleteCard(anyLong())).thenReturn(responseDTO);

        mockMvc.perform(delete("/api/v1/cards/{cardId}", 11L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("test"));
    }

    @Test
    void deleteCard_shouldReturn400_whenWrongRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/cards/{cardId}", -11L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCard_shouldReturn500_whenException() throws Exception {
        when(cardApiService.deleteCard(anyLong())).thenThrow(new RuntimeException());

        mockMvc.perform(delete("/api/v1/cards/{cardId}", 11L))
                .andExpect(status().isInternalServerError());
    }



}
