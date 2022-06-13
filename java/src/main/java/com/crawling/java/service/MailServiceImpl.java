package com.crawling.java.service;

import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service("mailService")
public class MailServiceImpl implements MailService {

    static final String FROM = "abby@artistchai.co.kr";
    static final String FROMNAME = "김아별";
    static final String TO = "abby@artistchai.co.kr";
    static final String SMTP_USERNAME = "domain@artistchai.co.kr";
    static final String SMTP_PASSWORD = "abcd123456";

    static final String HOST = "smtp.mailplug.co.kr";
    static final int PORT = 465;

    static final String SUBJECT = "메일 제목";

    static final String BODY = String.join(
            System.getProperty("line.separator"),
            "<h1>메일 내용</h1>",
            "<p>테스트메일</p>"
    );

    @Override
    public void sendMail() throws Exception {

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.ssl.trust","smtp.mailplug.co.kr");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.debug", "true");

        Session session = Session.getDefaultInstance(props);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(TO));
        msg.setSubject(SUBJECT);
        msg.setContent(BODY, "text/html;charset=UTF-8");

        Transport transport = session.getTransport();
        try {
            System.out.println("Sending...");

            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
            transport.sendMessage(msg, msg.getAllRecipients());

            System.out.println("Email sent!");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            transport.close();
        }
    }




}
