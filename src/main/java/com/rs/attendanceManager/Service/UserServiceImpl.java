package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Entity.User;
import com.rs.attendanceManager.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepo userRepo;

    @Override
    public Optional<User> fetchUserBygrNo(String grNo) {
       return userRepo.findById(grNo);
    }
}
