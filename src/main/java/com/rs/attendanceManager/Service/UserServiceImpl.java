package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Dto.UserDto;
import com.rs.attendanceManager.Entity.User;
import com.rs.attendanceManager.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepo userRepo;

    @Override
    public UserDto fetchUserBygrNo(String grNo) {
       User user = userRepo.findById(grNo).
               orElseThrow(() -> new RuntimeException("User Not Found"));

       UserDto userDto = new UserDto();

       userDto.setGrNo(user.getGrNo());
       userDto.setName(user.getName());
       userDto.setDepartment(user.getDepartment());
       userDto.setSubDepartment(user.getSubDepartment());
       userDto.setTotalAttendance(user.getTotalAttendance());
       userDto.setMobileNumber(user.getMobileNumber());
       userDto.setArea(user.getArea());
       userDto.setAge(user.getAge());
       userDto.setInitiated(user.getIsInitiated());
       userDto.setEmail(user.getEmail());
       userDto.setRemarks(user.getRemarks());

       if(user.getPhoto() != null) {
           String base64 = Base64.getEncoder().
                   encodeToString(user.getPhoto());

           userDto.setPhotoBase64(base64);
       }
       return userDto;
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
