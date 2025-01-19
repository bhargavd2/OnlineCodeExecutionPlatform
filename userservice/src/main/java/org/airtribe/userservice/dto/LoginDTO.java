package org.airtribe.userservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    @NotEmpty(message = "must have email")
    private String email;
    @NotEmpty(message = "must have password")
    private String password;
}

