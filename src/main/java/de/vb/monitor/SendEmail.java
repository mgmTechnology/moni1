package de.vb.monitor;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;


public class SendEmail {



    public static void sendNotificationMail(Properties appProps, String email_subject, String email_body) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", appProps.getProperty("mail.server"));
        properties.put("mail.smtp.port", appProps.getProperty("mail.port"));
        String finalFrom = appProps.getProperty("mail.from");
        String finalPassword = appProps.getProperty("mail.pw");
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(finalFrom, finalPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(appProps.getProperty("mail.from")));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(appProps.getProperty("mail.to")));
            message.setSubject(email_subject);
            message.setText(email_body);

            Transport.send(message);
            System.out.print("X");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
