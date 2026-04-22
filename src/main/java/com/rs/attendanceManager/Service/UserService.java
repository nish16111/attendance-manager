package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Dto.CreateUserRequest;
import com.rs.attendanceManager.Dto.UpdateUserRequest;
import com.rs.attendanceManager.Dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto fetchUserByGrNo(String grNo);

    UserDto createUser(CreateUserRequest request);

    UserDto updateUser(String grNo, UpdateUserRequest request);

    void deleteUser(String grNo);

    List<UserDto> fetchAllUsers();
}
