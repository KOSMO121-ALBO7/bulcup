package com.bulcup;

import java.util.HashMap;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Component
public class Email {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        return javaMailSender;
    }
    /**
    * 이메일 발송 함수
    * @param title 이메일 제목
    * @param to 받는 사람
    * @param templateName 이메일 템플릿
    * @param values 이메일에 들어가는 값
    */
	public void Send(String title, String to, String templateName, HashMap<String, String> values) {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        
		try {
			
			helper = new MimeMessageHelper(message, true);
			
			// 발신자 설정
			helper.setFrom("gt-choe@naver.com");
			
			//메일 제목 설정
	        helper.setSubject(title);

	        //수신자 설정
	        helper.setTo(to);

	        //템플릿에 전달할 데이터 설정
	        Context context = new Context();
	        values.forEach((key, value)->{
	            context.setVariable(key, value);
	        });

	        //메일 내용 설정 : 템플릿 프로세스
	        String html = templateEngine.process(templateName, context);
	        helper.setText(html, true);

	        //메일 보내기
	        javaMailSender.send(message);
	        
		} catch (MessagingException e) {
			e.printStackTrace();
		}
        
    }
}
