package com.vault.controller;

import com.vault.dto.UserResponse;
import com.vault.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/u/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.listAll());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toResponse(userService.getById(id)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/users/{id}/disable")
    public ResponseEntity<Map<String, Object>> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/users/{id}/enable")
    public ResponseEntity<Map<String, Object>> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/users/{id}/logout")
    public ResponseEntity<Map<String, Object>> logoutUser(@PathVariable Long id) {
        // Mock: invalidate refresh tokens would happen here
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        userService.updateRole(id, body.get("role"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/users/{id}/grant-api-access")
    public ResponseEntity<Map<String, Object>> grantApiAccess(@PathVariable Long id) {
        userService.grantApiAccess(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/users/{id}/revoke-api-access")
    public ResponseEntity<Map<String, Object>> revokeApiAccess(@PathVariable Long id) {
        userService.revokeApiAccess(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
