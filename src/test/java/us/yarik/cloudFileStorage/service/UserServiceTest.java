package us.yarik.cloudFileStorage.service;

import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import us.yarik.cloudFileStorage.model.User;
import us.yarik.cloudFileStorage.repository.UserRepository;

import java.util.Optional;
import java.util.WeakHashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static us.yarik.cloudFileStorage.service.UserServiceTest.TestResources.*;

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
    void givenNewEmail_registerCheck_shouldSave() {
        User NEW_USER = User.builder()
                .id(USER_ID)
                .name(NAME)
                .surname(SURNAME)
                .email("newEmail@gmail.com")
                .password(PASSWORD)
                .build();

        when(userRepository.findByEmail(NEW_USER.getEmail()))
                .thenReturn(Optional.empty());

        userService.registerCheck(NEW_USER);

        verify(userRepository, times(1)).save(NEW_USER);
    }

    @Test
    void givenExistEmail_registerCheck_shouldThrowException() {
        when(userRepository.findByEmail(USER.getEmail()))
                .thenReturn(Optional.of(USER));
        assertThatThrownBy(() -> {
            userService.registerCheck(USER);
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exist.");
    }

    @Test
    void findByEmail_shouldReturnUserIfExist() {
        when(userRepository.findByEmail(USER.getEmail()))
                .thenReturn(Optional.of(USER));
        Optional<User> user = userRepository.findByEmail(USER.getEmail());
        assertTrue(user.isPresent());
    }

    @Test
    void findByEmail_shouldReturnEmpty() {
        when(userRepository.findByEmail(USER.getEmail()))
                .thenReturn(Optional.empty());
        Optional<User> user = userRepository.findByEmail(USER.getEmail());
        assertTrue(user.isEmpty());
    }

    @Test
    void registerCheck_shouldHashPasswordBeforeSaving(){
        when(userRepository.findByEmail(USER.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD))
                .thenReturn("hashedPassword");

        userService.registerCheck(USER);

        verify(passwordEncoder, times(1)).encode(PASSWORD);
        assertEquals("hashedPassword", USER.getPassword());
        verify(userRepository, times(1)).save(USER);
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
    }
}
