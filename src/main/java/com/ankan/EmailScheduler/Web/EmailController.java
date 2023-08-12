package com.ankan.EmailScheduler.Web;

import com.ankan.EmailScheduler.Entity.EmailRequest;
import com.ankan.EmailScheduler.Entity.EmailResponse;
import com.ankan.EmailScheduler.Job.EmailJob;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class EmailController {

    @Autowired
    private Scheduler scheduler;

    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());

            if (dateTime.isBefore(ZonedDateTime.now())) {
                EmailResponse emailResponse = new EmailResponse(false, "Date time should be after current time");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emailResponse);
            }

            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail, dateTime);

            scheduler.scheduleJob(jobDetail, trigger);
            EmailResponse emailResponse = new EmailResponse(true, jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email scheduled successfully");
            return ResponseEntity.ok(emailResponse);


        } catch (SchedulerException sE) {
            log.error("Error while scheduling email", sE);
            EmailResponse emailResponse = new EmailResponse(false, "Error while scheduling email");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
        }
    }

    private JobDetail buildJobDetail(EmailRequest scheduleEmailRequest) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", scheduleEmailRequest.getEmailId());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class) //takes parameter which is the main job class.
                .withIdentity(UUID.randomUUID().toString(), "email-jobs").withDescription("Send an email job").usingJobData(jobDataMap).storeDurably() //this tell quartz to store the job even without a trigger and persist them in db.
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime start_at) {

        return TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(jobDetail.getKey().getName(), "email-trigger")
                .withDescription("send email trigger")
                .startAt(Date.from(start_at.toInstant()))
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForTotalCount5).withMisfireHandlingInstructionFireNow()) // it handles misfire and then fire it again
                .build();

    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("running");
    }
}

