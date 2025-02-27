package hexlet.code.demo.controller;

import hexlet.code.demo.dto.AuthRequest;
import hexlet.code.demo.util.JWTUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthenticationController {
    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public String createToken(@Valid @RequestBody AuthRequest authRequest) {
        try {
            var authentication = new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), authRequest.getPassword());
            authenticationManager.authenticate(authentication);
            var token = jwtUtils.generateToken(authRequest.getUsername());
            return token;
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Email or password is incorrect"
            );
        }
    }
}
