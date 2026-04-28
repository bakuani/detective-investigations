package com.detective.backend.controller;

import com.detective.backend.domain.User;
import com.detective.backend.domain.Solution;
import com.detective.backend.repository.UserRepository;
import com.detective.backend.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final SolutionRepository solutionRepository;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@AuthenticationPrincipal User user, @RequestBody java.util.Map<String, String> updateData) {
        User dbUser = userRepository.findById(user.getId()).orElse(null);
        if (dbUser == null) return ResponseEntity.notFound().build();
        if (updateData.containsKey("username")) {
            dbUser.setUsername(updateData.get("username"));
        }
        if (updateData.containsKey("bio")) {
             dbUser.setBio(updateData.get("bio"));
        }
        userRepository.save(dbUser);
        return ResponseEntity.ok(dbUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/solutions")
    public ResponseEntity<List<Solution>> getUserSolutions(@PathVariable Long id) {
        return ResponseEntity.ok(solutionRepository.findByUserId(id));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
