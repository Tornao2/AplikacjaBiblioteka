package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.StaffRegistrationRequest;
import com.biblioteka.backend.dto.UserDTO;
import com.biblioteka.backend.entity.Staff;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.entity.UserRole;
import com.biblioteka.backend.repository.StaffRepository;
import com.biblioteka.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {
    @Mock private StaffRepository staffRepository;
    @Mock private UserRepository userRepository;
    @Mock private SystemLogService logService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserService userService;
    @InjectMocks
    private StaffService staffService;

    @Test
    @DisplayName("Create: Hasło powinno zostać zakodowane przed zapisem")
    void shouldEncodePasswordWhenCreatingStaff() {
        StaffRegistrationRequest request = new StaffRegistrationRequest();
        request.setUsername("worker");
        request.setPassword("rawPassword");
        request.setSalary(4000.0);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(staffRepository.save(any(Staff.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userService.mapToDTO(any())).thenReturn(new UserDTO());
        staffService.createStaff(request);
        verify(passwordEncoder).encode("rawPassword");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encodedPassword", userCaptor.getValue().getPassword());
    }

    @Test
    @DisplayName("Update: Błąd gdy pracownik o podanym ID nie istnieje")
    void updateStaffShouldThrowExceptionWhenNotFound() {
        when(staffRepository.findById(99L)).thenReturn(Optional.empty());
        StaffRegistrationRequest request = new StaffRegistrationRequest();
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> staffService.updateStaff(99L, request));
        assertEquals("Pracownik nie istnieje", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update: Nie zmieniaj hasła, jeśli w request jest puste")
    void updateStaffShouldNotChangePasswordIfEmpty() {
        User existingUser = User.builder().password("oldHash").build();
        Staff existingStaff = Staff.builder().user(existingUser).build();
        when(staffRepository.findById(1L)).thenReturn(Optional.of(existingStaff));
        when(staffRepository.save(any(Staff.class))).thenReturn(existingStaff);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userService.mapToDTO(any())).thenReturn(new UserDTO());
        StaffRegistrationRequest request = new StaffRegistrationRequest();
        request.setPassword("");
        request.setSalary(5000.0);
        staffService.updateStaff(1L, request);
        assertEquals("oldHash", existingUser.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Remove: Degradacja roli do 'Czytelnik' po usunięciu z personelu")
    void removeStaffKeepUserShouldDegradeRole() {
        User user = User.builder()
                .id(10L)
                .firstName("Adam")
                .lastName("Kowalski")
                .role(UserRole.Bibliotekarz)
                .build();
        Staff staff = Staff.builder().id(1L).user(user).build();
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        staffService.removeStaffKeepUser(1L);
        assertEquals(UserRole.Czytelnik, user.getRole());
        assertNull(user.getStaffDetails());
        verify(userRepository).save(user);
        verify(staffRepository).delete(staff);
        verify(logService).addLog(any(), eq("STAFF_REMOVED"), contains("Adam Kowalski"), eq("WARNING"));
    }

    @Test
    @DisplayName("Create: Domyślna rola 'Bibliotekarz' gdy nie podano innej")
    void shouldSetDefaultRoleWhenNotProvided() {
        StaffRegistrationRequest request = new StaffRegistrationRequest();
        request.setRole(null);
        request.setSalary(3000.0);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(staffRepository.save(any(Staff.class))).thenAnswer(i -> i.getArguments()[0]);
        staffService.createStaff(request);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserRole.Bibliotekarz, userCaptor.getValue().getRole());
    }
}