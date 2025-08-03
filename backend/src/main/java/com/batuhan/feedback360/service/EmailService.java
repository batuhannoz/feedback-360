package com.batuhan.feedback360.service;

import com.batuhan.feedback360.model.entitiy.EmailLog;
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.enums.EmailStatus;
import com.batuhan.feedback360.model.enums.EmailTemplate;
import com.batuhan.feedback360.repository.EmailLogRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MessageHandler messageHandler;
    private final EmailLogRepository emailLogRepository;

    @Value("${app.invitation.base-url}")
    private String invitationBaseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendInvitationEmail(String to, String token) {
        String invitationLink = invitationBaseUrl + "?token=" + token + "&email=" + to;
        String subject = messageHandler.getMessage("email.invitation.subject");
        String body = messageHandler.getMessage("email.invitation.body", invitationLink);

        sendAndLogEmail(to, subject, body, EmailTemplate.INVITATION, null);
    }

    @Async
    public void sendPasswordResetEmail(User user, String token) {
        String passwordResetLink = invitationBaseUrl + "/reset-password?token=" + token;
        String subject = messageHandler.getMessage("email.password-reset.subject");
        String body = messageHandler.getMessage("email.passwordReset.body", passwordResetLink);

        sendAndLogEmail(user.getEmail(), subject, body, EmailTemplate.PASSWORD_RESET, user);
    }

    @Async
    public void sendPeriodStartedEmail(String to, String periodName) {
        String subject = messageHandler.getMessage("email.period.start.subject", periodName);
        String body = messageHandler.getMessage("email.period.start.body", periodName);

        sendAndLogEmail(to, subject, body, EmailTemplate.PERIOD_STARTED, null);
    }

    private void sendAndLogEmail(String to, String subject, String body, EmailTemplate template, User user) {
        EmailLog.EmailLogBuilder logBuilder = EmailLog.builder()
            .toEmail(to)
            .template(template)
            .user(user)
            .sentAt(LocalDateTime.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            logBuilder.status(EmailStatus.SENT);
            log.info("Email sent successfully to {}. Template: {}", to, template);

        } catch (MailException e) {
            logBuilder.status(EmailStatus.FAILED);
            logBuilder.errorMessage(e.getMessage());
            log.error("Failed to send email to {}. Template: {}. Error: {}", to, template, e.getMessage());
        } finally {
            emailLogRepository.save(logBuilder.build());
        }
    }
}