package hexlet.code.demo.service;

import hexlet.code.demo.config.EncodersConfig;
import hexlet.code.demo.dto.UserDTO.UserCreateDTO;
import hexlet.code.demo.dto.UserDTO.UserDTO;
import hexlet.code.demo.dto.UserDTO.UserUpdateDTO;
import hexlet.code.demo.exception.ResourceNotFoundException;
import hexlet.code.demo.mapper.UserMapper;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public final class UserService {
    private final EncodersConfig encodersConfig;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        var result = users.stream()
                .map(userMapper::map)
                .toList();
        return result;
    }

    public UserDTO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + id + " not found"));
        var dto = userMapper.map(user);
        return dto;
    }

    public UserDTO create(UserCreateDTO userData) {
        User user = userMapper.map(userData);
        PasswordEncoder encoder = encodersConfig.passwordEncoder();
        String cryptedPassword = encoder.encode(user.getPassword());
        user.setPassword(cryptedPassword);
        userRepository.save(user);
        var dto = userMapper.map(user);
        return dto;
    }

    public UserDTO update(UserUpdateDTO userData, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + id + " not found"));
        if (userData.getPassword() != null && userData.getPassword().isPresent()) {
            PasswordEncoder encoder = encodersConfig.passwordEncoder();
            var cryptedPassword = encoder.encode(userData.getPassword().get());
            user.setPassword(cryptedPassword);
        }
        userMapper.update(userData, user);
        userRepository.save(user);
        var dto = userMapper.map(user);
        return dto;
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
