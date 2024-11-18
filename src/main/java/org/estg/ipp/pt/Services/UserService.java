package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;  // Ensure this is injected correctly by Spring
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * Registers a new user.
     *
     * @param user The user to register.
     * @return 1 if the user was registered successfully, 0 if the username or email already exists.
     */
    public int register(User user) {
        // Check if the username already exists
        if (userRepository.existsByName(user.getName())) {
            System.out.println("Username already exists");
            return 0;
        }

        // Check if email already exists (requires an additional repository method)
        if (userRepository.existsByEmail(user.getEmail())) { // Make sure to define this method in the repository
            System.out.println("Email already exists");
            return 0;
        }

        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save the user in the database
        userRepository.save(user);
        System.out.println("User registered successfully");
        return 1;
    }

    /**
     * Authenticates a user by username or email and password.
     *
     * @param usernameOrEmail The username or email of the user.
     * @param password        The password to validate.
     * @return The authenticated user, or null if authentication fails.
     */
    public User authenticate(String usernameOrEmail, String password) {
        // Find user by name
        Optional<User> userOptional = userRepository.findByName(usernameOrEmail);

        // If not found by name, check by email (requires an additional repository method)
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(usernameOrEmail); // Define findByEmail in the repository
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if the provided password matches the stored password
            if (passwordEncoder.matches(password, user.getPassword())) {
                System.out.println("Authentication successful");
                return user;
            } else {
                System.out.println("Invalid password");
            }
        } else {
            System.out.println("User not found");
        }
        return null;
    }

    public User getUserByName(String username) {
           Optional<User> user = userRepository.findByName(username);
        return user.orElse(null);
    }
}
