package org.airtribe.userservice.controller;

import org.airtribe.userservice.dto.CodeRequestDto;
import org.airtribe.userservice.dto.ExecutionResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/execute")
public class ExecuteController {

    @Value("${CodeExecutionServiceApplication.api.ip}")
    private String apiIp;

    @Value("${CodeExecutionServiceApplication.api.port}")
    private int apiPort;

    @Value("${CodeExecutionServiceApplication.api.path}")
    private String apiPath;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<Object> getResult(@RequestBody CodeRequestDto codeRequestDto)
    {
        Map<String, Object> response = new HashMap<>();
        try {
            String apiUrl = "http://" + apiIp + ":" + apiPort + apiPath;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            codeRequestDto.setCode(new String(Base64.getEncoder().encodeToString(codeRequestDto.getCode().getBytes(StandardCharsets.UTF_8))));
            HttpEntity<CodeRequestDto> entity = new HttpEntity<>(codeRequestDto, headers);
            ResponseEntity<ExecutionResultDto> apiResponse = restTemplate.exchange(apiUrl,
                    HttpMethod.POST,
                    entity,
                    ExecutionResultDto.class);
            if(apiResponse.getStatusCode() != HttpStatus.OK)
            {
                response.put("Status", "503");
                return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
            }
            ExecutionResultDto resultDto = apiResponse.getBody();
            response.put("Status", "201");
            response.put("result", resultDto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e)
        {
            response.put("Status", "500");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
