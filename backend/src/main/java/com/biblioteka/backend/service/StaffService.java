package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.StaffDTO;
import com.biblioteka.backend.dto.StaffRegistrationRequest;
import com.biblioteka.backend.entity.Staff;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.entity.UserRole;
import com.biblioteka.backend.repository.StaffRepository;
import com.biblioteka.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SystemLogService logService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffDTO createStaff(StaffRegistrationRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if (user.isEmpty()){
            User actualUser = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .role(request.getRole() != null ? request.getRole() : UserRole.Bibliotekarz)
                    .build();
            user = Optional.of(userRepository.save(actualUser));
        }
        Staff staff = Staff.builder()
                .user(user.get())
                .phoneNumber(request.getPhoneNumber())
                .hireDate(request.getHireDate())
                .salary(BigDecimal.valueOf(request.getSalary()))
                .build();
        Staff savedStaff = staffRepository.save(staff);
        String username = getCurrentUsername();
        logService.addLog(username, "STAFF_CREATED",
                "Dodano pracownika: " + user.get().getFullName(), "INFO");
        return mapToDTO(savedStaff);
    }

    @Transactional
    public StaffDTO updateStaff(Long id, StaffRegistrationRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pracownik nie istnieje"));
        User user = staff.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setSalary(BigDecimal.valueOf(request.getSalary()));
        staff.setHireDate(request.getHireDate());
        userRepository.save(user);
        Staff updated = staffRepository.save(staff);
        String username = getCurrentUsername();
        logService.addLog(username, "STAFF_UPDATED",
                "Zaktualizowano dane: " + user.getFullName(), "INFO");

        return mapToDTO(updated);
    }

    @Transactional
    public void removeStaffKeepUser(Long staffId) {
        staffRepository.findById(staffId).ifPresent(staff -> {
            User user = staff.getUser();
            String fullName = user.getFullName();
            user.setRole(UserRole.Czytelnik);
            user.setStaffDetails(null);
            staff.setUser(null);
            userRepository.save(user);
            staffRepository.delete(staff);
            String username = getCurrentUsername();
            logService.addLog(username, "STAFF_REMOVED",
                    "Zakończono zatrudnienie: " + fullName + ". Konto użytkownika aktywne.", "WARNING");
        });
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private StaffDTO mapToDTO(Staff entity) {
        return StaffDTO.builder()
                .id(entity.getId())
                .user(userService.mapToDTO(entity.getUser()))
                .phoneNumber(entity.getPhoneNumber())
                .hireDate(entity.getHireDate())
                .salary(entity.getSalary().doubleValue())
                .build();
    }
}