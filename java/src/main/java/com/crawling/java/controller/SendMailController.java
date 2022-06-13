package com.crawling.java.controller;

import com.crawling.java.service.MailServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SendMailController {

    private final MailServiceImpl mailService;

    private static Log logger = LogFactory.getLog(SendMailController.class);

    public SendMailController(MailServiceImpl mailService) {
        this.mailService = mailService;
    }

    @GetMapping(value = "/sendmail")
    public String sendmail() throws Exception {

        mailService.sendMail();

        return "mail.html";

    }



}
