package com.example.bankcards.service;

import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApiService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {
        log.info("Получение пользователя по id={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new UserNotFoundException("Пользователь не найден");
                });
        log.info("Пользователь с id={} успешно найден", userId);

        return mapToDTO(user);
    }

    public void deleteUser(Long userId) {
        log.info("Удаление пользователя id={}", userId);
        userRepository.deleteById(userId);
        log.info("Пользователь id={} успешно удален", userId);
    }

    @Transactional(readOnly = true)
    public PagedResponseDTO<UserResponseDTO> findAllUsers(Integer page, Integer size, String login, LocalDate createdAtFrom, LocalDate createdAtTo) {
        log.info("Получение списка пользователей");
        Specification<User> specification = Specification.allOf(
                UserSpecification.hasLogin(login),
                UserSpecification.createdAtAfter(createdAtFrom),
                UserSpecification.createdAtBefore(createdAtTo)
        );
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findAll(specification, pageable);
        log.info("Найдено {} пользователей", users.getTotalElements());

        return mapToPagedDTO(users);
    }

    private PagedResponseDTO<UserResponseDTO> mapToPagedDTO(Page<User> users) {
        List<UserResponseDTO> usersDTO = users.stream()
                .map(this::mapToDTO)
                .toList();

        return new PagedResponseDTO<>(usersDTO, users.getNumber(), users.getSize(), users.getTotalElements());
    }

    private UserResponseDTO mapToDTO(User user) {

        return new UserResponseDTO(user.getId(), user.getLogin(), user.getRole());
    }
}
