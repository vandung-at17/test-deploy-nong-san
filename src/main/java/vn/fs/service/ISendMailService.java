package vn.fs.service;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.stereotype.Service;

import vn.fs.model.dto.MailInfo;

/**
 * @author DongTHD
 *
 */
@Service
public interface ISendMailService {
	void run();

	void queue(String to, String subject, String body);

	void queue(MailInfo mail);

	void send(MailInfo mail) throws MessagingException, IOException;
}
