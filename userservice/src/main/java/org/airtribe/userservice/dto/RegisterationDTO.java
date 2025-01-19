package org.airtribe.userservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RegisterationDTO {
    @NotEmpty(message = "must have username")
    private String username;
    @NotEmpty(message = "must have email")
    private String email;
    @NotEmpty(message = "must have password")
    private String password;
}
