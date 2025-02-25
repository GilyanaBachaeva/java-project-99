package hexlet.code.demo;

import hexlet.code.demo.controller.UsersController;
import hexlet.code.demo.dto.UserDTO;
import hexlet.code.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UsersControllerTest {

    @InjectMocks
    private UsersController usersController;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("John Doe");

        when(userService.createUser(any(UserDTO.class))).thenReturn(userDTO);

        UserDTO createdUser = usersController.create(userDTO);

        assertEquals("John Doe", createdUser.getFirstName());
        verify(userService, times(1)).createUser(userDTO);
    }

    @Test
    public void testGetUserById() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John Doe");

        when(userService.getUserById(1L)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = usersController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getFirstName());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    public void testGetAllUsers() {
        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setFirstName("John Doe");

        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setFirstName("Jane Doe");

        List<UserDTO> users = Arrays.asList(user1, user2);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = usersController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void testUpdateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John Doe");

        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = usersController.updateUser(1L, userDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getFirstName());
        verify(userService, times(1)).updateUser(eq(1L), any(UserDTO.class));
    }

    @Test
    public void testDeleteUser() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = usersController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(1L);
    }
}
