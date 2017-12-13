package polibee.core.emails;

public interface IEmailSender {
    boolean sendEmail(String from, String to, String[] cc, String[] bcc, String subject, String htmlContent, String attachment);
}
