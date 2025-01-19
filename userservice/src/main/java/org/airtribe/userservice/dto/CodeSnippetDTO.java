package org.airtribe.userservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CodeSnippetDTO {
    private Long codeId;
    private String language;
    private String code;
    private String result;
    private LocalDateTime timestamp;
    private boolean shareable;
}
