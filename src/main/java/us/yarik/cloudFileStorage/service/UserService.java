package us.yarik.cloudFileStorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import us.yarik.cloudFileStorage.exception.ConflictException;
import us.yarik.cloudFileStorage.model.User;
import us.yarik.cloudFileStorage.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void save(User user){
        userRepository.save(user);
    }

    public void registerCheck(User user) {
          if(userRepository.findByEmail(user.getEmail()).isPresent()){
              throw new ConflictException("Email already exist.");
          }
          user.setPassword(passwordEncoder.encode(user.getPassword()));
          userRepository.save(user);
    }
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

}
