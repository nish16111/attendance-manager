package com.rs.attendanceManager.Controller;

import com.rs.attendanceManager.Entity.User;
import com.rs.attendanceManager.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/home/users/")

public class HomeController {

    @Autowired
    UserService userService;

    @GetMapping("fetchUserBygrNo")
    public ResponseEntity<?> fetchUserBygrNo(@RequestParam String grNo) {
        Optional<User> user = userService.fetchUserBygrNo(grNo);
//        if(user.isPresent()) {
//            return ResponseEntity.ok(user.get());
//        } else {
//            return ResponseEntity.status(404).body("User with grNo: " + grNo + " not found");
//        }
        try {
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User with grNo: " + grNo + " not found");

            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while fetching user: " + e.getMessage());
        }
    }

    @PostMapping("createUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            User userResponse = userService.createUser(user);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating user: " + e.getMessage());
        }
    }

    
}
