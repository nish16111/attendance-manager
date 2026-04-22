package com.rs.attendanceManager.Service;

import com.rs.attendanceManager.Dto.CreateUserRequest;
import com.rs.attendanceManager.Dto.UpdateUserRequest;
import com.rs.attendanceManager.Dto.UserDto;
import com.rs.attendanceManager.Entity.User;
import com.rs.attendanceManager.Repo.UserRepo;
import com.rs.attendanceManager.exception.ConflictException;
import com.rs.attendanceManager.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto fetchUserByGrNo(String grNo) {
        User user = userRepo.findById(grNo)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for grNo: " + grNo));
        return new UserDto(user);
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        if (userRepo.existsById(request.getGrNo())) {
            throw new ConflictException("User already exists for grNo: " + request.getGrNo());
        }

        User user = new User();
        user.setGrNo(request.getGrNo().trim());
        applyUserFields(request, user);

        User saved = userRepo.save(user);
        return new UserDto(saved);
    }

    @Override
    public UserDto updateUser(String grNo, UpdateUserRequest request) {
        User existingUser = userRepo.findById(grNo)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for grNo: " + grNo));

        existingUser.setName(request.getName().trim());
        existingUser.setDepartment(request.getDepartment().trim());
        existingUser.setSubDepartment(request.getSubDepartment().trim());
        existingUser.setTotalAttendance(request.getTotalAttendance());
        existingUser.setMobileNumber(request.getMobileNumber().trim());
        existingUser.setArea(request.getArea().trim());
        existingUser.setAge(request.getAge());
        existingUser.setIsInitiated(request.getIsInitiated());
        existingUser.setRemarks(request.getRemarks());
        existingUser.setEmail(request.getEmail() == null ? null : request.getEmail().trim().toLowerCase());

        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            existingUser.setPhoto(readPhotoBytes(request.getPhoto()));
        }

        User updated = userRepo.save(existingUser);
        return new UserDto(updated);
    }

    @Override
    public void deleteUser(String grNo) {
        if (!userRepo.existsById(grNo)) {
            throw new ResourceNotFoundException("User not found for grNo: " + grNo);
        }
        userRepo.deleteById(grNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> fetchAllUsers() {
        return userRepo.findAll().stream().map(UserDto::new).toList();
    }

    private void applyUserFields(CreateUserRequest request, User user) {
        user.setName(request.getName().trim());
        user.setDepartment(request.getDepartment().trim());
        user.setSubDepartment(request.getSubDepartment().trim());
        user.setTotalAttendance(request.getTotalAttendance());
        user.setMobileNumber(request.getMobileNumber().trim());
        user.setArea(request.getArea().trim());
        user.setAge(request.getAge());
        user.setIsInitiated(request.getIsInitiated());
        user.setRemarks(request.getRemarks());
        user.setEmail(request.getEmail() == null ? null : request.getEmail().trim().toLowerCase());

        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            user.setPhoto(readPhotoBytes(request.getPhoto()));
        }
    }

    private byte[] readPhotoBytes(MultipartFile photo) {
        try {
            return photo.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read uploaded photo");
        }
    }
}
