package hexlet.code.demo.controller;

import hexlet.code.demo.dto.UserDTO.UserCreateDTO;
import hexlet.code.demo.dto.UserDTO.UserDTO;
import hexlet.code.demo.dto.UserDTO.UserUpdateDTO;
import hexlet.code.demo.service.UserService;
import hexlet.code.demo.util.UserUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserUtils userUtils;

    @GetMapping()
    public ResponseEntity<List<UserDTO>> index() {
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(userService.getAll()))
                .body(userService.getAll());
    }

    @GetMapping("/{id}")
    public UserDTO show(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO userData) {
        var dto = userService.create(userData);
        return dto;
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userUtils.isCurrentUser(#id)")
    public UserDTO update(@Valid @RequestBody UserUpdateDTO userData, @PathVariable Long id) {
        var dto = userService.update(userData, id);
        return dto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@userUtils.isCurrentUser(#id)")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
