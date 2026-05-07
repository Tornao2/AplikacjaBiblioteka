package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.UserDTO;
import com.biblioteka.backend.dto.UserRegistrationRequest;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.repository.LoanRepository;
import com.biblioteka.backend.repository.StaffRepository;
import com.biblioteka.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SystemLogService logService;
    private final LoanRepository loanRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Login zajęty!");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();
        User saved = userRepository.save(user);
        String username = getCurrentUsername();
        logService.addLog(username, "USER_CREATED",
                "Utworzono użytkownika: " + saved.getUsername(), "INFO");
        return mapToDTO(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje."));
        boolean hasActiveLoans = loanRepository.findAll().stream()
                .anyMatch(loan -> loan.getUserId().equals(id) && loan.getReturnDate() == null);
        boolean isStaff = staffRepository.findAll().stream()
                .anyMatch(staff -> staff.getUser().equals(user));
        if (isStaff) {
            throw new RuntimeException("Nie można usunąć konto pracownika bez pierwszej zmiany roli.");
        }
        if (hasActiveLoans) {
            throw new RuntimeException("Nie można usunąć użytkownika, który ma aktywne wypożyczenia.");
        }
        userRepository.delete(user);
        String username = getCurrentUsername();
        logService.addLog(username, "USER_DELETED",
                "Usunięto użytkownika: " + user.getUsername(), "INFO");
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    @Transactional
    public void updateCurrentEmail(String newEmail) {
        String currentUsername = getCurrentUsername();
        if ("SYSTEM".equals(currentUsername)) {
            throw new RuntimeException("Brak autoryzacji.");
        }
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje."));
        user.setEmail(newEmail);
        userRepository.save(user);
        logService.addLog(currentUsername, "USER_UPDATE", "Zaktualizowano adres email", "INFO");
    }

    @Transactional
    public void updateCurrentPassword(String currentPassword, String newPassword) {
        String currentUsername = getCurrentUsername();
        if ("SYSTEM".equals(currentUsername)) {
            throw new RuntimeException("Brak autoryzacji.");
        }
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje."));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Obecne hasło jest niepoprawne.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logService.addLog(currentUsername, "USER_UPDATE", "Zmieniono hasło użytkownika", "INFO");
    }

    @Transactional
    public void deleteCurrentAccount() {
        String currentUsername = getCurrentUsername();
        if ("SYSTEM".equals(currentUsername)) {
            throw new RuntimeException("Brak autoryzacji.");
        }
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje."));
        boolean hasActiveLoans = loanRepository.findAll().stream()
                .anyMatch(loan -> loan.getUserId().equals(user.getId()) && loan.getReturnDate() == null);
        if (hasActiveLoans) {
            throw new RuntimeException("Nie możesz usunąć konta, ponieważ posiadasz aktywne wypożyczenia.");
        }
        userRepository.delete(user);
        logService.addLog(currentUsername, "USER_DELETED", "Użytkownik usunął swoje konto", "INFO");
    }

    public UserDTO mapToDTO(User entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .role(entity.getRole())
                .build();
    }
}