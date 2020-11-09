package com.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Service
public class MailService{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JavaMailSender mailSender;

    private final MailConfig mailConfig;

    private final TemplateEngine templateEngine;

    public MailService(JavaMailSender mailSender, MailConfig mailConfig, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.mailConfig = mailConfig;
        this.templateEngine = templateEngine;
    }

    /**
     * 发送文本邮件
     */
    void sendSimpleMail(final String url) {
        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailConfig.getFromAddress());
        message.setTo(mailConfig.getToAddress());
        message.setSubject(mailConfig.getSubject());
        message.setText(mailConfig.getContent() + "【" + url + "】");

        try {
            mailSender.send(message);
            logger.warn("邮件发送成功，访问地址：{}", url);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("邮件发送失败，访问地址：{}", url);
        }
    }

    /**
     * 发送html邮件
     */
    void sendHtmlMail(String urls) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            final Context context = new Context();
            context.setVariable("url", urls);
            final String emailContent = templateEngine.process("emailTemplate", context);

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailConfig.getFromAddress());
            helper.setTo(mailConfig.getToAddress());
            helper.setSubject(mailConfig.getSubject());
            helper.setText(emailContent, true);

            mailSender.send(message);
            logger.warn("邮件发送成功，访问地址：{}", urls);
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.warn("邮件发送失败，访问地址：{}", urls);
        }
    }


    /**
     * 发送带附件的邮件
     */
    public void sendAttachmentsMail(String to, String subject, String content, String filePath){
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailConfig.getFromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
            helper.addAttachment(fileName, file);
            //helper.addAttachment("test"+fileName, file);

            mailSender.send(message);
            logger.info("带附件的邮件已经发送。");
        } catch (MessagingException e) {
            logger.error("发送带附件的邮件时发生异常！", e);
        }
    }


    /**
     * 发送正文中有静态资源（图片）的邮件
     */
    public void sendInlineResourceMail(String to, String subject, String content, String rscPath, String rscId){
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailConfig.getFromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource res = new FileSystemResource(new File(rscPath));
            helper.addInline(rscId, res);

            mailSender.send(message);
            logger.info("嵌入静态资源的邮件已经发送。");
        } catch (MessagingException e) {
            logger.error("发送嵌入静态资源的邮件时发生异常！", e);
        }
    }
}
