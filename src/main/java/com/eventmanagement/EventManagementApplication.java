package com.eventmanagement;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Event Category Management Application",
                version = "v1.0",
                description = "API for Event Category Management Application [by Chibuike Okeke]",
                contact = @Contact(
                        name = "Chibuike Okeke",
                        email = "offxtiancee@gmail.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://github.com/cbuike"
                )
        )
)
@SpringBootApplication
public class EventManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagementApplication.class, args);
    }

}
