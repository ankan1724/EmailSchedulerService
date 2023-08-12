package com.ankan.EmailScheduler.Entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @Email
    @NotEmpty
    private String emailId;

    @NotEmpty
    private String subject;

    @NotEmpty
    private String body;

    @NotNull
    private LocalDateTime dateTime;


    @NotNull
    private ZoneId timeZone;
}
