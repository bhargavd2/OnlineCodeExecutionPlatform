package org.airtribe.userservice.dto;

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
