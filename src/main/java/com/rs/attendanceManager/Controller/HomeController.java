package com.rs.attendanceManager.Controller;

import com.rs.attendanceManager.Dto.UserDto;
import com.rs.attendanceManager.Entity.User;
import com.rs.attendanceManager.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/home/users/")
public class HomeController {

    @Autowired
    UserService userService;

    @GetMapping("fetchUserBygrNo")
    public ResponseEntity<?> fetchUserBygrNo(@RequestParam String grNo) {

        try {
            return ResponseEntity.ok(userService.fetchUserBygrNo(grNo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while fetching user: " + e.getMessage());
        }
    }

    @PostMapping("createUser")
    public ResponseEntity<?> createUser(
            @RequestParam("grNo") String grNo,
            @RequestParam("name") String name,
            @RequestParam("department") String department,
            @RequestParam("subDepartment") String subDepartment,
            @RequestParam("totalAttendance") BigDecimal totalAttendance,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("mobileNumber") String mobileNumber,
            @RequestParam("area") String area,
            @RequestParam("age") int age,
            @RequestParam("isInitiated") boolean isInitiated,
            @RequestParam("remarks") String remarks,
            @RequestParam("email") String email
    ) {
        try {
            User user = new User();
            user.setGrNo(grNo);
            user.setName(name);
            user.setDepartment(department);
            user.setSubDepartment(subDepartment);
            user.setTotalAttendance(totalAttendance);
            user.setPhoto(photo.getBytes());
            user.setMobileNumber(mobileNumber);
            user.setAge(age);
            user.setArea(area);
            user.setIsInitiated(isInitiated);
            user.setRemarks(remarks);
            user.setEmail(email);

            User userResponse = userService.createUser(user);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating user: " + e.getMessage());
        }
    }

    @PutMapping("updateUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        try{
            User userResponse = userService.updateUser(user);
            if(userResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while updating user: " + e.getMessage());
        }
    }

    @DeleteMapping("deleteUser")
    public ResponseEntity<?> deleteUser(@RequestParam String grNo) {
        try{
            userService.deleteUser(grNo);
            return ResponseEntity.ok("User with grNo: " + grNo + " removed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting the user: " + e.getMessage());
        }
    }
}
