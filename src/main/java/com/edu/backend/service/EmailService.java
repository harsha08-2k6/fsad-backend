package com.edu.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String frontendBaseUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:no-reply@edu.local}") String fromEmail,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void sendOtp(String toEmail, String otp, long expiryMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Password Reset OTP");
        message.setText(
                "Your password reset OTP is: " + otp + "\n" +
                "This code expires in " + expiryMinutes + " minutes."
        );
        mailSender.send(message);
    }

    public void sendApprovalMail(String toEmail, String role, String password) {
        String loginUrl = frontendBaseUrl + "/login";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Registration Approved");
        message.setText(
                "Your " + role + " account has been approved.\n" +
                "Login here: " + loginUrl + "\n" +
                "Email: " + toEmail + "\n" +
                "Password: " + password
        );
        mailSender.send(message);
    }

    public void sendRejectionMail(String toEmail, String role) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Registration Update");
        message.setText(
                "Your " + role + " registration request was not approved at this time.\n" +
                "If you believe this is a mistake, please contact support."
        );
        mailSender.send(message);
    }
}
