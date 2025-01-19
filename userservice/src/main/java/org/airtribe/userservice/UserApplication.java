package org.airtribe.userservice;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Bean
    public ApplicationRunner printEndpoints(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return args -> {

            System.out.println("Exposed endpoints:");
            requestMappingHandlerMapping
                    .getHandlerMethods()
                    .forEach((key, value) -> {
                        System.out.println(key.getMethodsCondition()+ "\t:\t"
                                +key.getPatternValues().toArray()[0] + "\t->\t" + value);
                    });
        };
    }
}
