package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Entity.User;
import jakarta.validation.Valid;

import java.util.Optional;

public interface UserService {

    Optional<User> fetchUserBygrNo(String grNo);

    User createUser(@Valid User user);
}
