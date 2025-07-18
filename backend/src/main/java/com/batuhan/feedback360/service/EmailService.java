package com.batuhan.feedback360.service;

import com.batuhan.feedback360.util.MessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    public void sendPasswordResetEmail(com.batuhan.feedback360.model.entitiy.User user, String token) {
        String passwordResetLink = invitationBaseUrl + "?token=" + token + "&email=" + user.getEmail();
        String subject = messageHandler.getMessage("email.passwordReset.subject");
        String body = messageHandler.getMessage("email.passwordReset.body", passwordResetLink);
    }
}