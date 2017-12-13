package polibee.core.emails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: Đây chỉ là Simple Email Service; full email service cần
 * - gửi được email theo template
 * - có thể gửi được cho nhiều transporter khác nhau (hiện luôn sử dụng AWS SES qua default sender)
 * - tính năng mở rộng có thể là kiểm soát transporter để lựa chọn kênh gửi
 * - handle email black-list (để tránh tăng bounce rate)
 * - khi gửi failed thì phải có cơ chế retry hoặc lưu lại việc gửi fail
 */
@Service
public class SimpleEmailService implements IEmailService {
    private IEmailSender _defaultSender;

    @Autowired
    public SimpleEmailService(IEmailSender sender) {
        this._defaultSender = sender;
    }

    @Override
    public boolean sendEmail(String from, String to, String[] cc, String[] bcc, String subject, String htmlContent, String attachment) {
        return this._defaultSender.sendEmail(from, to, cc, bcc, subject, htmlContent, attachment);
    }
}
