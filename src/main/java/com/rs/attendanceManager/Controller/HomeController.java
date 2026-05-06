package com.rs.attendanceManager.Controller;

import com.rs.attendanceManager.Dto.BulkUserImportResponse;
import com.rs.attendanceManager.Dto.CreateUserRequest;
import com.rs.attendanceManager.Dto.UpdateUserRequest;
import com.rs.attendanceManager.Dto.UserDto;
import com.rs.attendanceManager.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/users")
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{grNo}")
    public ResponseEntity<UserDto> fetchUserByGrNo(@PathVariable String grNo) {
        return ResponseEntity.ok(userService.fetchUserByGrNo(grNo));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<UserDto> createUser(@Valid @ModelAttribute CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    public ResponseEntity<BulkUserImportResponse> importUsers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.importUsers(file));
    }

    @PutMapping(value = "/{grNo}", consumes = {"multipart/form-data"})
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String grNo,
            @Valid @ModelAttribute UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(grNo, request));
    }

    @DeleteMapping("/{grNo}")
    public ResponseEntity<Void> deleteUser(@PathVariable String grNo) {
        userService.deleteUser(grNo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> fetchAllUsers() {
        return ResponseEntity.ok(userService.fetchAllUsers());
    }
}
