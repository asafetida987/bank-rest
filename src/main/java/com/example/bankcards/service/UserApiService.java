package com.example.bankcards.service;

import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserApiService {

    private final UserRepository userRepository;

    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        return mapToDTO(user);
    }

    public void deleteUser(Long userId) {

        userRepository.deleteById(userId);
    }

    public PagedResponseDTO<UserResponseDTO> findAllUsers(Integer page, Integer size, String login, LocalDate createdAtFrom, LocalDate createdAtTo) {
        Specification<User> specification = Specification.allOf(
                UserSpecification.hasLogin(login),
                UserSpecification.createdAtAfter(createdAtFrom),
                UserSpecification.createdAtBefore(createdAtTo)
        );

        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findAll(specification, pageable);

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
