package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Dto.BulkUserImportResponse;
import com.rs.attendanceManager.Entity.User;
import com.rs.attendanceManager.Repo.UserRepo;
import com.rs.attendanceManager.exception.ConflictException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        userService = new UserServiceImpl(userRepo, validator);
    }

    @Test
    void importUsersSavesValidCsvRows() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                """
                grNo,name,department,subDepartment,totalAttendance,photo,mobileNumber,area,age,isInitiated,remarks,email
                GR001,John Doe,IT,Support,87.50,,9999999999,Mumbai,22,true,Good attendance,John@example.com
                GR002,Jane Doe,HR,Recruitment,92.00,,8888888888,Pune,25,false,,JANE@example.com
                """.getBytes(StandardCharsets.UTF_8)
        );

        when(userRepo.findAllById(any())).thenReturn(List.of());
        when(userRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BulkUserImportResponse response = userService.importUsers(file);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<User>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(userRepo).saveAll(captor.capture());
        List<User> savedUsers = StreamSupport.stream(captor.getValue().spliterator(), false).toList();

        assertThat(response.getImportedCount()).isEqualTo(2);
        assertThat(response.getImportedGrNos()).containsExactly("GR001", "GR002");
        assertThat(savedUsers).hasSize(2);
        assertThat(savedUsers.get(0).getEmail()).isEqualTo("john@example.com");
        assertThat(savedUsers.get(1).getEmail()).isEqualTo("jane@example.com");
        assertThat(savedUsers.get(0).getIsInitiated()).isTrue();
        assertThat(savedUsers.get(1).getIsInitiated()).isFalse();
    }

    @Test
    void importUsersRejectsExistingGrNoValues() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                """
                grNo,name,department,subDepartment,totalAttendance,mobileNumber,area,age,isInitiated
                GR001,John Doe,IT,Support,87.50,9999999999,Mumbai,22,true
                """.getBytes(StandardCharsets.UTF_8)
        );

        User existingUser = new User();
        existingUser.setGrNo("GR001");

        when(userRepo.findAllById(any())).thenReturn(List.of(existingUser));

        assertThatThrownBy(() -> userService.importUsers(file))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("GR001");

        verify(userRepo, never()).saveAll(any());
    }
}
