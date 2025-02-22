package us.yarik.CloudFileStorage.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Surname cannot be empty!")
    @Size(min = 2, max = 64, message = "Surname must be between 2 and 64 characters!")
    private String surname;

    @NotBlank(message = "Name cannot be empty!")
    @Size(min = 2, max = 64, message = "Name must be between 2 and 64 characters!")
    private String name;

    @NotBlank(message = "Email cannot be empty!")
    @Email(message = "Invalid email format!")
    private String email;

    @NotBlank(message = "Password cannot be empty!")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters!")
    private String password;
}