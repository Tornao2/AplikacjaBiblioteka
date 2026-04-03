package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.StaffDTO;
import com.biblioteka.backend.dto.StaffRegistrationRequest;
import com.biblioteka.backend.entity.Staff;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.entity.UserRole;
import com.biblioteka.backend.repository.StaffRepository;
import com.biblioteka.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SystemLogService logService;

    @Transactional(readOnly = true)
    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffDTO createStaff(StaffRegistrationRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(UserRole.LIBRARIAN)
                .build();
        User savedUser = userRepository.save(user);
        Staff staff = Staff.builder()
                .user(savedUser)
                .phoneNumber(request.getPhoneNumber())
                .hireDate(request.getHireDate())
                .salary(BigDecimal.valueOf(request.getSalary()))
                .build();
        Staff savedStaff = staffRepository.save(staff);
        logService.addLog("SYSTEM", "STAFF_CREATED",
                "Dodano pracownika: " + savedUser.getFullName(), "INFO");
        return mapToDTO(savedStaff);
    }

    @Transactional
    public void removeStaffKeepUser(Long staffId) {
        staffRepository.findById(staffId).ifPresent(staff -> {
            String fullName = staff.getUser().getFullName();
            User user = staff.getUser();
            user.setRole(UserRole.USER);
            userRepository.save(user);
            staffRepository.delete(staff);
            logService.addLog("SYSTEM", "STAFF_REMOVED",
                    "Zakończono zatrudnienie: " + fullName + ". Konto użytkownika aktywne.", "WARNING");
        });
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