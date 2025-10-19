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
import com.example.bankcards.repository.specification.CardSpecification;
import com.example.bankcards.util.CardUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardApiService {

    private final CardUserService cardUserService;
    private final CardAdminService cardAdminService;

    public PagedResponseDTO<CardResponseDTO> getMyCards(User currentUser, Integer page, Integer size,
                                                        LocalDate expiryDateFrom, LocalDate expiryDateTo,
                                                        CardStatus status, BigDecimal balanceFrom,
                                                        BigDecimal balanceTo, Boolean isRequestBlock) {

        Specification<Card> specification = getSpecification(currentUser,null, expiryDateFrom, expiryDateTo,
                status, balanceFrom, balanceTo, isRequestBlock);

        Pageable pageable = PageRequest.of(page, size);

        Page<Card> cards = cardUserService.getMyCards(specification, pageable);

        return mapToPagedDTO(cards);
    }


    public BalanceResponseDTO getBalanceByCard(User currentUser, Long cardId) {
        CardBalance cardBalance = cardUserService.getCardBalance(currentUser, cardId);

        return mapToBalanceDTO(cardBalance);
    }

    @Transactional
    public MessageResponseDTO transfer(User currentUser, TransferRequestDTO transferRequestDTO) {
        cardUserService.transferMoney(currentUser, transferRequestDTO.cardIdFrom(), transferRequestDTO.cardIdTo(), transferRequestDTO.amount());

        return new MessageResponseDTO("Перевод осуществлен успешно");
    }

    @Transactional
    public MessageResponseDTO requestBlock(User currentUser, CardRequestDTO cardRequestDTO) {
        cardUserService.requestBlock(currentUser, cardRequestDTO.cardId());

        return new MessageResponseDTO("Запрос блокировки успешно отправлен");
    }

    public PagedResponseDTO<CardResponseDTO> getAllCards(Integer page, Integer size, String userLogin,
                                                         LocalDate expiryDateFrom, LocalDate expiryDateTo,
                                                         CardStatus status, BigDecimal balanceFrom,
                                                         BigDecimal balanceTo, Boolean isRequestBlock) {
        Specification<Card> specification = getSpecification(null, userLogin, expiryDateFrom, expiryDateTo,
                status, balanceFrom, balanceTo, isRequestBlock);
        Pageable pageable = PageRequest.of(page, size);

        Page<Card> cards = cardAdminService.getAll(specification, pageable);

        return mapToPagedDTO(cards);

    }

    @Transactional
    public CardResponseDTO createNewCard(NewCardRequestDTO requestDTO) {
        Card card = cardAdminService.create(requestDTO.ownerLogin());

        return mapToDTO(card);
    }

    @Transactional
    public MessageResponseDTO blockCard(CardRequestDTO cardRequestDTO) {
        cardAdminService.blockCard(cardRequestDTO.cardId());

        return new MessageResponseDTO("Карта успешно заблокирована");
    }

    @Transactional
    public MessageResponseDTO activateCard(CardRequestDTO cardRequestDTO) {
        cardAdminService.activateCard(cardRequestDTO.cardId());

        return new MessageResponseDTO("Карта успешно активирована");
    }

    @Transactional
    public MessageResponseDTO deleteCard(Long cardId) {
        cardAdminService.deleteCard(cardId);

        return new MessageResponseDTO("Карта успешно удалена");
    }

    private Specification<Card> getSpecification(User currentUser, String userLogin, LocalDate expiryDateFrom, LocalDate expiryDateTo,
                                                 CardStatus status, BigDecimal balanceFrom,
                                                 BigDecimal balanceTo, Boolean isRequestBlock) {
        return Specification.allOf(
                CardSpecification.cardOwner(currentUser),
                CardSpecification.hasUserLogin(userLogin),
                CardSpecification.expiryDateAfter(expiryDateFrom),
                CardSpecification.expiryDateBefore(expiryDateTo),
                CardSpecification.hasStatus(status),
                CardSpecification.balanceFrom(balanceFrom),
                CardSpecification.balanceTo(balanceTo),
                CardSpecification.isRequestBlock(isRequestBlock)

        );
    }

    private PagedResponseDTO<CardResponseDTO> mapToPagedDTO(Page<Card> cards) {
        List<CardResponseDTO> dtos = cards.stream()
                .map(this::mapToDTO)
                .toList();

        return new PagedResponseDTO<>(dtos, cards.getNumber(), cards.getSize(), cards.getTotalElements());
    }

    private CardResponseDTO mapToDTO(Card card) {

        return new CardResponseDTO(card.getId(), CardUtil.maskingNumber(card.getCardNumber()), card.getOwner().getLogin(), card.getExpiryDate(), card.getCardStatus());
    }

    private BalanceResponseDTO mapToBalanceDTO(CardBalance cardBalance) {

        return new BalanceResponseDTO(cardBalance.getBalance());
    }
}
