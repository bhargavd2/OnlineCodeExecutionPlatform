package org.airtribe.userservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CodeRequestDto {
    private String language;
    private String code;
}
