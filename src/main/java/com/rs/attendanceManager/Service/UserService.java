package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Dto.UserDto;
import com.rs.attendanceManager.Entity.User;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {

    UserDto fetchUserBygrNo(String grNo);

    User createUser(@Valid User user);

    User updateUser(@Valid User user);

    void deleteUser(String grNo);
}
