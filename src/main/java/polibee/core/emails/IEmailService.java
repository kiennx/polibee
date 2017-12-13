package polibee.core.emails;

import org.springframework.stereotype.Service;

/**
 * Skeleton of an email service
 */
@Service
public interface IEmailService {
    boolean sendEmail(String from, String to, String[] cc, String[] bcc, String subject, String htmlContent, String attachment);
}
