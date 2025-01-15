package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Entity.User;

import java.util.Optional;

public interface UserService {

    Optional<User> fetchUserBygrNo(String grNo);
}
