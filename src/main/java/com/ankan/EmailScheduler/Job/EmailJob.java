package com.ankan.EmailScheduler.Job;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EmailJob extends QuartzJobBean {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MailProperties mailProperties;

    //....this comes from Job interface...//
//    @Override
//    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//
//    }

    //....this comes from implementing class of Job interface i.e QuartzJobBean...//
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        // we get jobdatamap using JobExecutionContext obj

        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String recipientEmail = jobDataMap.getString("email");
        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");

        sendMail(mailProperties.getUsername(), recipientEmail, subject, body);
    }

    private void sendMail(String fromEmail, String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
            messageHelper.setSubject(subject);
            messageHelper.setText(body, true);
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(toEmail);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error(String.valueOf(e));
        }
    }
}
