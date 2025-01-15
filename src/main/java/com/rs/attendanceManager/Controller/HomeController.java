package com.rs.attendanceManager.Controller;

import com.rs.attendanceManager.Entity.User;
import com.rs.attendanceManager.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController

public class HomeController {

    @Autowired
    UserService userService;

    @GetMapping("/fetchUserBygrNo")
    public ResponseEntity<?> fetchUserBygrNo(@RequestParam String grNo) {
        Optional<User> user = userService.fetchUserBygrNo(grNo);
        if(user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(404).body("User with grNo: " + grNo + " not found");
        }

    }
}
