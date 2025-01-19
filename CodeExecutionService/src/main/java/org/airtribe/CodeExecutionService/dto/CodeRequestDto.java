package org.airtribe.CodeExecutionService.dto;

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
