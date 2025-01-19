package org.airtribe.CodeExecutionService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExecutionResultDto {
    private String output;
    private String error;
}
