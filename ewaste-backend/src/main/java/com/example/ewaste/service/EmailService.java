package com.example.ewaste.service;

import com.example.ewaste.model.PickupRequest;
import com.example.ewaste.model.User;
import com.example.ewaste.model.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;
    @Value("${app.admin.email:admin@ewaste.com}")
    private String adminEmail;


    // -------------------- HELPER METHOD --------------------
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // enable HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send email to " + to, e);
        }
    }

    // -------------------- SEND OTP EMAIL --------------------
    public void sendOtpEmail(PickupRequest request) {
        String otp = request.getOtp(); // ✅ read OTP from entity

        String html = """
                <html>
                <body style="font-family:Arial,sans-serif; background-color:#f4f4f4; margin:0; padding:0;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color:#fff; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                                    <tr>
                                        <td style="background-color:#FF9800; color:#fff; text-align:center; padding:20px; font-size:24px;">
                                            🔑 OTP Verification
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:30px; color:#333; font-size:16px; line-height:1.6;">
                                            <p>Hello <strong>%s</strong>,</p>
                                            <p>Your pickup request <strong>#%d</strong> requires confirmation. Please use the OTP below:</p>
                                            <p style="text-align:center; font-size:28px; font-weight:bold; margin:20px 0; color:#FF9800;">%s</p>
                                            <p>This OTP is valid for 10 minutes.</p>
                                            <p style="margin-top:30px;">Best regards,<br><strong>The E-Waste Team</strong></p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color:#eeeeee; color:#666; text-align:center; padding:15px; font-size:14px;">
                                            &copy; 2025 E-Waste Services. All rights reserved.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                request.getUser().getUsername(),
                request.getId(),
                otp
        );

        sendHtmlEmail(request.getUser().getEmail(), "OTP for Pickup Confirmation", html);
    }
    // -------------------- REQUEST SUBMITTED EMAIL --------------------
    public void sendRequestSubmittedEmail(PickupRequest request) {
        String html = """
            <html>
            <body style="font-family:Arial,sans-serif; background-color:#f4f4f4; margin:0; padding:0;">
                <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" 
                                style="background-color:#fff; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                                <tr>
                                    <td style="background-color:#2196F3; color:#fff; text-align:center; padding:20px; font-size:24px;">
                                        📦 Pickup Request Submitted
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:30px; color:#333; font-size:16px; line-height:1.6;">
                                        <p>Hello <strong>%s</strong>,</p>
                                        <p>Your pickup request has been submitted successfully.</p>
                                        <p><strong>Request ID:</strong> #%d</p>
                                        <p><strong>Device:</strong> %s (%s)</p>
                                        <p>Status: <strong style="color:#2196F3;">%s</strong></p>
                                        <p>You will receive updates once your request is processed.</p>
                                        <p style="margin-top:30px;">Best regards,<br><strong>The E-Waste Team</strong></p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background-color:#eeeeee; color:#666; text-align:center; padding:15px; font-size:14px;">
                                        &copy; 2025 E-Waste Services. All rights reserved.
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                request.getUser().getUsername(),
                request.getId(),
                request.getDeviceType(),
                request.getModel(),
                request.getStatus()
        );

        sendHtmlEmail(request.getUser().getEmail(), "Pickup Request Submitted Successfully", html);
    }
    // -------------------- ADMIN NEW REQUEST NOTIFICATION --------------------
    public void sendAdminNotificationEmail(PickupRequest request) {
        String html = """
            <html>
            <body style="font-family:Arial,sans-serif; background-color:#f4f4f4; margin:0; padding:0;">
                <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" 
                                style="background-color:#fff; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                                <tr>
                                    <td style="background-color:#673AB7; color:#fff; text-align:center; padding:20px; font-size:24px;">
                                        📢 New Pickup Request Submitted
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:30px; color:#333; font-size:16px; line-height:1.6;">
                                        <p>A new pickup request has been submitted:</p>
                                        <ul>
                                            <li><strong>Request ID:</strong> #%d</li>
                                            <li><strong>User:</strong> %s (%s)</li>
                                            <li><strong>Device:</strong> %s (%s)</li>
                                            <li><strong>Quantity:</strong> %d</li>
                                            <li><strong>Status:</strong> %s</li>
                                        </ul>
                                        <p>Please login to the admin dashboard to review.</p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background-color:#eeeeee; color:#666; text-align:center; padding:15px; font-size:14px;">
                                        &copy; 2025 E-Waste Services. All rights reserved.
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                request.getId(),
                request.getUser().getUsername(),
                request.getUser().getEmail(),
                request.getDeviceType(),
                request.getModel(),
                request.getQuantity(),
                request.getStatus()
        );

        sendHtmlEmail(adminEmail, "New Pickup Request Submitted", html);
    }

    // -------------------- WELCOME EMAIL --------------------
    public void sendWelcomeEmail(User user) {
        String html = """
                <html>
                <body style="font-family:Arial,sans-serif; background-color:#f4f4f4; margin:0; padding:0;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color:#fff; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                                    <tr>
                                        <td style="background-color:#4CAF50; color:#fff; text-align:center; padding:20px; font-size:24px;">
                                            🎉 Welcome to E-Waste!
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:30px; color:#333; font-size:16px; line-height:1.6;">
                                            <p>Hello <strong>%s</strong>,</p>
                                            <p>Thank you for joining our platform. Start recycling your old devices and help save the planet ♻️</p>
                                            <p style="text-align:center; margin-top:30px;">
                                                <a href="http://localhost:8084" style="background-color:#4CAF50; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; font-weight:bold;">Visit Dashboard</a>
                                            </p>
                                            <p style="margin-top:30px;">Best regards,<br><strong>The E-Waste Team</strong></p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color:#eeeeee; color:#666; text-align:center; padding:15px; font-size:14px;">
                                            &copy; 2025 E-Waste Services. All rights reserved.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(user.getUsername());

        sendHtmlEmail(user.getEmail(), "Welcome to E-Waste!", html);
    }

// -------------------- STATUS UPDATE EMAIL --------------------
    public void sendStatusUpdateEmail(PickupRequest request) {
        String statusColor;
        switch (request.getStatus()) {
            case PICKED_UP -> statusColor = "#4CAF50"; // Green
            case REJECTED -> statusColor = "#F44336"; // Red
            case SCHEDULED -> statusColor = "#FF9800"; // Orange
            default -> statusColor = "#2196F3"; // Blue
        }

        String html = """
                <html>
                <body style="font-family:Arial,sans-serif; background-color:#f4f4f4; margin:0; padding:0;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" 
                                    style="background-color:#fff; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                                    <tr>
                                        <td style="background-color:#2196F3; color:#fff; text-align:center; padding:20px; font-size:24px;">
                                            🚀 Pickup Status Update
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:30px; color:#333; font-size:16px; line-height:1.6;">
                                            <p>Hello <strong>%s</strong>,</p>
                                            <p>Your pickup request <strong>#%d</strong> status has been updated to:</p>
                                            <p style="text-align:center; font-size:20px; font-weight:bold; color:%s; margin:20px 0;">%s</p>
                                            %s
                                            <p style="margin-top:30px;">Best regards,<br><strong>The E-Waste Team</strong></p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color:#eeeeee; color:#666; text-align:center; padding:15px; font-size:14px;">
                                            &copy; 2025 E-Waste Services. All rights reserved.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                request.getUser().getUsername(),
                request.getId(),
                statusColor,
                request.getStatus(),
                request.getStatus() == RequestStatus.REJECTED ?
                        "<p style='color:#F44336;'>Reason: " + request.getRejectionReason() + "</p>" : ""
        );

        sendHtmlEmail(request.getUser().getEmail(), "Your Pickup Request Status Update", html);
    }

    // -------------------- PICKUP SCHEDULED EMAIL --------------------
    public void sendPickupScheduledEmail(PickupRequest request) {
        String html = """
                <html>
                <body style="font-family:Arial,sans-serif; background-color:#f4f4f4; margin:0; padding:0;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" 
                                    style="background-color:#fff; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                                    <tr>
                                        <td style="background-color:#FF9800; color:#fff; text-align:center; padding:20px; font-size:24px;">
                                            📅 Pickup Scheduled
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:30px; color:#333; font-size:16px; line-height:1.6;">
                                            <p>Hello <strong>%s</strong>,</p>
                                            <p>Your pickup request <strong>#%d</strong> has been scheduled:</p>
                                            <p style="text-align:center; margin:10px 0;">
                                                <strong>Date:</strong> %s<br>
                                                <strong>Time:</strong> %s
                                            </p>
                                            <p style="margin-top:30px;">Best regards,<br><strong>The E-Waste Team</strong></p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color:#eeeeee; color:#666; text-align:center; padding:15px; font-size:14px;">
                                            &copy; 2025 E-Waste Services. All rights reserved.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                request.getUser().getUsername(),
                request.getId(),
                request.getPickupDate(),
                request.getPickupTime()
        );

        sendHtmlEmail(request.getUser().getEmail(), "Pickup Scheduled", html);
    }

    // -------------------- PICKUP ASSIGNMENT EMAIL --------------------
    public void sendPickupAssignmentEmail(String toEmail, PickupRequest request) {
        String html = """
                <html>
                <body style="font-family:Arial,sans-serif; background-color:#f4f4f4; margin:0; padding:0;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" 
                                    style="background-color:#fff; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                                    <tr>
                                        <td style="background-color:#2196F3; color:#fff; text-align:center; padding:20px; font-size:24px;">
                                            📬 New Pickup Assigned
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:30px; color:#333; font-size:16px; line-height:1.6;">
                                            <p>Hello,</p>
                                            <p>You have been assigned a new pickup request:</p>
                                            <ul>
                                                <li>Request ID: <strong>%d</strong></li>
                                                <li>Pickup Address: %s</li>
                                                <li>Scheduled Date: %s</li>
                                                <li>Pickup Time: %s</li>
                                            </ul>
                                            <p style="margin-top:30px;">Best regards,<br><strong>The E-Waste Team</strong></p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color:#eeeeee; color:#666; text-align:center; padding:15px; font-size:14px;">
                                            &copy; 2025 E-Waste Services. All rights reserved.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                request.getId(),
                request.getPickupAddress(),
                request.getPickupDate(),
                request.getPickupTime()
        );

        sendHtmlEmail(toEmail, "New Pickup Assigned", html);
    }

    // -------------------- ADMIN COMPLETION EMAIL --------------------
    public void sendCompletionEmail(PickupRequest request) {
        String pickupPerson = request.getAssignedPickupPerson() != null
                ? request.getAssignedPickupPerson().getName()
                : "Not assigned";

        String html = """
            <html>
            <body style="font-family:Arial,sans-serif; background-color:#f4f4f4; margin:0; padding:0;">
                <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" 
                                style="background-color:#fff; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                                <tr>
                                    <td style="background-color:#4CAF50; color:#fff; text-align:center; padding:20px; font-size:24px;">
                                        ✅ Pickup Completed
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:30px; color:#333; font-size:16px; line-height:1.6;">
                                        <p>Pickup request <strong>#%d</strong> for <strong>%s</strong> has been completed.</p>
                                        <p>Pickup Person: %s</p>
                                        <p style="margin-top:30px;">Best regards,<br><strong>The E-Waste Team</strong></p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background-color:#eeeeee; color:#666; text-align:center; padding:15px; font-size:14px;">
                                        &copy; 2025 E-Waste Services. All rights reserved.
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                request.getId(),
                request.getModel(),
                pickupPerson
        );

        sendHtmlEmail(adminEmail, "Pickup Request Completed", html);
    }

}
