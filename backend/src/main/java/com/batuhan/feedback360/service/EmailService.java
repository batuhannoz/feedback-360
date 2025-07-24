package com.batuhan.feedback360.service;

import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.util.MessageHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MessageHandler messageHandler;

    @Value("${app.invitation.base-url}")
    private String invitationBaseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendInvitationEmail(String to, String token) {
        String invitationLink = invitationBaseUrl + "?token=" + token + "&email=" + to;
        String subject = messageHandler.getMessage("email.invitation.subject");
        String body = messageHandler.getMessage("email.invitation.body", invitationLink);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(User user, String token) {
        String passwordResetLink = invitationBaseUrl + "/reset-password?token=" + token; // A more RESTful link
        String subject = messageHandler.getMessage("email.passwordReset.subject");
        String body = messageHandler.getMessage("email.passwordReset.body", passwordResetLink);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Async
    public void sendPeriodStartedEmail(String to, String periodName) {
        String subject = messageHandler.getMessage("email.period.start.subject", periodName);
        String body = messageHandler.getMessage("email.period.start.body", periodName);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Async
    public void sendReportShareEmail(String to, String periodName, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String subject = messageHandler.getMessage("email.report.share.subject", periodName);

        helper.setText(htmlBody, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom(fromEmail);

        mailSender.send(mimeMessage);
    }
}