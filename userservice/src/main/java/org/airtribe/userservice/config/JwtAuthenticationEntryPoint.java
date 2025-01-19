package org.airtribe.userservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.server.PathContainer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String path = request.getRequestURI();
        boolean pathExists = handlerMapping.getHandlerMethods().keySet().stream()
                .anyMatch(requestMappingInfo -> {
                    PathPatternParser parser = new PathPatternParser();
                    PathPattern pattern = parser.parse((String) requestMappingInfo.getPatternValues().toArray()[0]);
                    return pattern.matches(PathContainer.parsePath(path));
                });

        if (pathExists) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
        }
    }
}
