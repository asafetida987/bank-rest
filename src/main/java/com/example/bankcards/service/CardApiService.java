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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CardApiService {

    private final CardUserService cardUserService;
    private final CardAdminService cardAdminService;

    @Transactional(readOnly = true)
    public PagedResponseDTO<CardResponseDTO> getMyCards(User currentUser, Integer page, Integer size,
                                                        LocalDate expiryDateFrom, LocalDate expiryDateTo,
                                                        CardStatus status, BigDecimal balanceFrom,
                                                        BigDecimal balanceTo, Boolean isRequestBlock) {
        log.info("Пользователь id={} запрашивает свои карты", currentUser.getId());
        Specification<Card> specification = getSpecification(currentUser,null, expiryDateFrom, expiryDateTo,
                status, balanceFrom, balanceTo, isRequestBlock);

        Pageable pageable = PageRequest.of(page, size);

        Page<Card> cards = cardUserService.getMyCards(specification, pageable);
        log.info("Найдено {} карт для пользователя id={}", cards.getTotalElements(), currentUser.getId());

        return mapToPagedDTO(cards);
    }

    @Transactional(readOnly = true)
    public BalanceResponseDTO getBalanceByCard(User currentUser, Long cardId) {
        log.info("Пользователь id={} запрашивает баланс карты id={}", currentUser.getId(), cardId);
        CardBalance cardBalance = cardUserService.getCardBalance(currentUser, cardId);
        log.info("Баланс карты id={} успешно получен", cardId);

        return mapToBalanceDTO(cardBalance);
    }

    @Transactional
    public MessageResponseDTO transfer(User currentUser, TransferRequestDTO transferRequestDTO) {
        log.info("Пользователь id={} осуществялет перевод средств", currentUser.getId());
        cardUserService.transferMoney(currentUser, transferRequestDTO.cardIdFrom(), transferRequestDTO.cardIdTo(), transferRequestDTO.amount());
        log.info("Перевод средств пользователем id={} выполнен успешно", currentUser.getId());

        return new MessageResponseDTO("Перевод осуществлен успешно");
    }

    @Transactional
    public MessageResponseDTO requestBlock(User currentUser, CardRequestDTO cardRequestDTO) {
        log.info("Пользователь id={} запрашивает блокировку карты id={}", currentUser.getId(), cardRequestDTO.cardId());
        cardUserService.requestBlock(currentUser, cardRequestDTO.cardId());
        log.info("Запрос на блокировку карты id={} успешно выполнен", cardRequestDTO.cardId());

        return new MessageResponseDTO("Запрос блокировки успешно отправлен");
    }

    @Transactional(readOnly = true)
    public PagedResponseDTO<CardResponseDTO> getAllCards(Integer page, Integer size, String userLogin,
                                                         LocalDate expiryDateFrom, LocalDate expiryDateTo,
                                                         CardStatus status, BigDecimal balanceFrom,
                                                         BigDecimal balanceTo, Boolean isRequestBlock) {
        log.info("Админ запрашивает список всех карт");
        Specification<Card> specification = getSpecification(null, userLogin, expiryDateFrom, expiryDateTo,
                status, balanceFrom, balanceTo, isRequestBlock);
        Pageable pageable = PageRequest.of(page, size);

        Page<Card> cards = cardAdminService.getAll(specification, pageable);
        log.info("Найдено {} карт", cards.getTotalElements());

        return mapToPagedDTO(cards);

    }

    @Transactional
    public CardResponseDTO createNewCard(NewCardRequestDTO requestDTO) {
        log.info("Админ создает карту для пользователя id={}", requestDTO.ownerId());
        Card card = cardAdminService.create(requestDTO.ownerId());
        log.info("Карта id={} успешно создана для пользователя id={}", card.getId(), requestDTO.ownerId());

        return mapToDTO(card);
    }

    @Transactional
    public MessageResponseDTO blockCard(CardRequestDTO cardRequestDTO) {
        log.info("Админ блокирует карту id={}", cardRequestDTO.cardId());
        cardAdminService.blockCard(cardRequestDTO.cardId());
        log.info("Карта id={} успешно заблокирована", cardRequestDTO.cardId());

        return new MessageResponseDTO("Карта успешно заблокирована");
    }

    @Transactional
    public MessageResponseDTO activateCard(CardRequestDTO cardRequestDTO) {
        log.info("Админ активирует карту id={}", cardRequestDTO.cardId());
        cardAdminService.activateCard(cardRequestDTO.cardId());
        log.info("Карта id={} успешно активирована", cardRequestDTO.cardId());

        return new MessageResponseDTO("Карта успешно активирована");
    }

    @Transactional
    public MessageResponseDTO deleteCard(Long cardId) {
        log.info("Админ удаляет карту id={}", cardId);
        cardAdminService.deleteCard(cardId);
        log.info("Карта id={} успешно удалена", cardId);

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
