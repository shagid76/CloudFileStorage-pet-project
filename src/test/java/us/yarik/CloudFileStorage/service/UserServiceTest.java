package us.yarik.CloudFileStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import us.yarik.CloudFileStorage.advice.ConflictException;
import us.yarik.CloudFileStorage.model.CreateUserRequest;
import us.yarik.CloudFileStorage.model.User;
import us.yarik.CloudFileStorage.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static us.yarik.CloudFileStorage.service.UserServiceTest.TestResources.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void save_shouldCallUserRepository() {
        userService.save(USER);
        verify(userRepository, times(1)).save(USER);
    }

    @Test
    void givenNewEmail_registerCheck_shouldPass() {
        CreateUserRequest newUser = CreateUserRequest.builder()
                .name(NAME)
                .surname(SURNAME)
                .email("newEmail@gmail.com")
                .password(PASSWORD)
                .build();

        when(userRepository.existsByEmail(newUser.getEmail())).thenReturn(false);

        assertDoesNotThrow(() -> userService.registerCheck(newUser));

        verify(userRepository, times(1)).existsByEmail(newUser.getEmail());
    }

    @Test
    void givenExistEmail_registerCheck_shouldThrowException() {
        when(userRepository.existsByEmail(CREATE_USER_REQUEST.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.registerCheck(CREATE_USER_REQUEST))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already exists");

        verify(userRepository, times(1)).existsByEmail(CREATE_USER_REQUEST.getEmail());
    }

    @Test
    void findByEmail_shouldReturnUserIfExist() {
        when(userRepository.findByEmail(USER.getEmail()))
                .thenReturn(Optional.of(USER));

        User user = userService.findByEmail(USER.getEmail());

        assertNotNull(user);
        assertEquals(USER, user);
    }

    @Test
    void findByEmail_shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findByEmail(USER.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.findByEmail(USER.getEmail()));

        verify(userRepository, times(1)).findByEmail(USER.getEmail());
    }

    @Test
    void createUser_shouldEncodePasswordAndSave() {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .password("password")
                .build();

        User mappedUser = userService.mapToUser(request);
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(mappedUser);

        assertDoesNotThrow(() -> userService.createUser(request));

        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    static class TestResources {
        static final String NAME = "NAME";
        static final String SURNAME = "SURNAME";
        static final String EMAIL = "email@gmail.com";
        static final String PASSWORD = "PASSWORD";
        static final Integer USER_ID = 1;
        static final User USER = User.builder()
                .id(USER_ID)
                .name(NAME)
                .surname(SURNAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();
        static final CreateUserRequest CREATE_USER_REQUEST = CreateUserRequest.builder()
                .name(NAME)
                .surname(SURNAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();
    }
}