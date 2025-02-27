package hexlet.code.demo.controller;

import hexlet.code.demo.dto.UserCreateDTO;
import hexlet.code.demo.dto.UserDTO;
import hexlet.code.demo.dto.UserUpdateDTO;
import hexlet.code.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UsersController {
    @Autowired
    private UserService userService;

    @GetMapping()
    public List<UserDTO> index() {
        return userService.getAll();
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
    public UserDTO update(@Valid @RequestBody UserUpdateDTO userData, @PathVariable Long id) {
        var dto = userService.update(userData, id);
        return dto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
