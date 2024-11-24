package dev.coms4156.project.kebabcase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import dev.coms4156.project.kebabcase.controller.UserController;
import dev.coms4156.project.kebabcase.entity.UserEntity;
import dev.coms4156.project.kebabcase.repository.UserRepositoryInterface;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserControllerUnitTests {

  @Mock
  private UserRepositoryInterface userRepository;

  @InjectMocks
  private UserController userController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  private String hashPassword(String password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte b : encodedHash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error hashing password", e);
    }
  }

  @Test
  void testAuthenticate_Success() {
    // Arrange
    String email = "test@example.com";
    String password = "password123";
    String hashedPassword = hashPassword(password);

    UserEntity user = new UserEntity();
    user.setId(1);
    user.setEmailAddress(email);
    user.setPassword(hashedPassword);

    when(userRepository.findByEmailAddress(email)).thenReturn(Optional.of(user));

    // Act
    ResponseEntity<Integer> response = userController.authenticate(email, password);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(user.getId(), response.getBody());
    verify(userRepository, times(1)).findByEmailAddress(email);
  }

  @Test
  void testAuthenticate_EmailNotFound() {
    // Arrange
    String email = "missing@example.com";
    String password = "password123";

    when(userRepository.findByEmailAddress(email)).thenReturn(Optional.empty());

    // Act
    ResponseEntity<Integer> response = userController.authenticate(email, password);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(userRepository, times(1)).findByEmailAddress(email);
  }

  @Test
  void testAuthenticate_InvalidPassword() {
    // Arrange
    String email = "test@example.com";
    String correctPassword = "password123";
    String wrongPassword = "wrongpassword";
    String hashedCorrectPassword = hashPassword(correctPassword);

    UserEntity user = new UserEntity();
    user.setId(1);
    user.setEmailAddress(email);
    user.setPassword(hashedCorrectPassword);

    when(userRepository.findByEmailAddress(email)).thenReturn(Optional.of(user));

    // Act
    ResponseEntity<Integer> response = userController.authenticate(email, wrongPassword);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNull(response.getBody());
    verify(userRepository, times(1)).findByEmailAddress(email);
  }

  @Test
  void testAuthenticate_BlankEmailOrPassword() {
    // Act
    ResponseEntity<Integer> responseWithBlankEmail = userController.authenticate("", "password123");
    ResponseEntity<Integer> responseWithBlankPassword = userController.authenticate("test@example.com", "");

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, responseWithBlankEmail.getStatusCode());
    assertNull(responseWithBlankEmail.getBody());

    assertEquals(HttpStatus.BAD_REQUEST, responseWithBlankPassword.getStatusCode());
    assertNull(responseWithBlankPassword.getBody());

    verifyNoInteractions(userRepository);
  }

  @Test
  void testCreateUserSuccess() {
    String firstName = "Sue";
    String lastName = "Donym";
    String emailAddress = "notapseudonym@example.com";
    String password = "alias?notI";

    UserEntity user = new UserEntity();
    user.setId(100);

    when(userRepository.findByEmailAddress(
            emailAddress))
            .thenReturn(Optional.empty());

    when(userRepository.save(any(UserEntity.class))).thenReturn(user);

    ResponseEntity<?> response = userController.createUser(firstName,
            lastName, emailAddress, password);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertTrue(response.getBody().toString().contains(
            "User was added successfully! User ID: "));
    verify(userRepository, times(1)).save(any(UserEntity.class));
  }

  @Test
  void testCreateUserFailure() {
    String firstName = "Emily";
    String lastName = "Johnson";
    String emailAddress = "emily.johnson@example.com";
    String password = "password789";

    UserEntity user = new UserEntity();
    user.setEmailAddress(emailAddress);

    when(userRepository.findByEmailAddress(
            emailAddress))
            .thenReturn(Optional.of(user));

    ResponseEntity<?> response = userController.createUser(firstName,
            lastName, emailAddress, password);



    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(response.getBody().toString().contains(
            "There is an account already associated with "));
  }
  
}