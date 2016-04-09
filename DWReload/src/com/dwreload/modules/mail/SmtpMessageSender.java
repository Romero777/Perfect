package com.dwreload.modules.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * The SMTP sender of e-mail messages.
 */
public class SmtpMessageSender {

    public SmtpMessageSender() {
    }

    /**
     * Creates a new JavaMail session.
     *
     * @param smtpHost a smtp host.
     * @param smtpPort a smtp host.
     * @param username a username e-mail account.
     * @param password a password e-mail account.
     * @return a created session.
     */
    public Session createSession(String smtpHost, String smtpPort, String username, String password, String security)
    	{
        Properties props = new Properties();
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.host", smtpHost);
        
        if (security.contentEquals("SSL"))
			{
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.socketFactory.port", smtpPort);
			}
		else if (security.contentEquals("TLS"))
			{
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.", "true");
			}
        return Session.getDefaultInstance(props, createAuthenticator(username, password));
    }

    /**
     * Creates a MIME message.
     *
     * @param session       a JavaMail session.
     * @param subject       a subject of message.
     * @param from          a from address.
     * @param to            recipients.
     * @param recipientType type of recipients.
     * @return {@code MimeMessage}.
     * @throws MessagingException if an error occurs.
     */
    public MimeMessage createMimeMessage(Session session, String subject,
                                         String from, String to,
                                         Message.RecipientType recipientType)
            throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(recipientType, InternetAddress.parse(to));
        msg.setSubject(subject);
        msg.setContent(new MimeMultipart());
        return msg;
    }

    /**
     * Adds a text in a message.
     *
     * @param message a message.
     * @param text    a text.
     * @param charset a charset of text.
     * @param type    a type of text (html, plain ...).
     * @return {@code MimeMessage} with a text,
     * @throws IOException        if an error occurs.
     * @throws MessagingException if an error occurs.
     */
    public MimeMessage addText(MimeMessage message, String text,
                               String charset, String type)
            throws IOException, MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(text, charset, type);
        MimeMultipart multipart = (MimeMultipart) message.getContent();
        multipart.addBodyPart(textPart);
        return message;
    }

    /**
     * Attaches a file to the message.
     *
     * @param message a message.
     * @param file    a file for attaching.
     * @return {@code MimeMessage} with a attached file.
     * @throws IOException        if an error occurs.
     * @throws MessagingException if an error occurs.
     */
    public MimeMessage addAttachment(MimeMessage message, File file)
            throws IOException, MessagingException {
        MimeBodyPart filePart = new MimeBodyPart();
        filePart.attachFile(file);
        MimeMultipart multipart = (MimeMultipart) message.getContent();
        multipart.addBodyPart(filePart);
        return message;
    }

    /**
     * Sends a MIME message.
     *
     * @param message a message.
     * @throws MessagingException if are errors.
     */
    public void sendMimeMessage(MimeMessage message) throws MessagingException {
        Transport.send(message);
    }

    /**
     * Creates a JavaMail {@link Authenticator}.
     *
     * @param username a username e-mail account.
     * @param password a password e-mail account.
     * @return a new {@link Authenticator}.
     */
    private Authenticator createAuthenticator(final String username,
                                              final String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }
}
