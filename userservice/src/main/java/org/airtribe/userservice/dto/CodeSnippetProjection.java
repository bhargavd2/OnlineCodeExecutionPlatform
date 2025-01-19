package org.airtribe.userservice.dto;

import java.time.LocalDateTime;

public interface CodeSnippetProjection {
    Long getCodeId();
    String getLanguage();
    boolean isShareable();
    LocalDateTime getTimestamp();
}
