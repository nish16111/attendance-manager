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

    @Override
    public User createUser(User user) {
        return userRepo.save(user);
    }

    @Override
    public User updateUser(User user) {
        Optional<User> existingUserById = userRepo.findById(user.getGrNo());

        if(existingUserById.isPresent()) {
            return userRepo.save(user);
        } else{
            return null;
        }
    }

    @Override
    public void deleteUser(String grNo) {
        userRepo.deleteById(grNo);
    }
}
