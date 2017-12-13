package polibee.core.emails;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import polibee.core.boot.ConfigHandler;
import polibee.core.boot.InvalidConfigException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Transporter giúp gửi email thông qua dịch vụ AWS SES
 * TODO: implement nhiều trường hợp phức tạp hơn hiện tại simple quá:
 * - handle tần suất gửi
 * - handle gửi email bằng text content
 * - handle attachment
 * - có các Overload sử dụng ít tham số hơn
 */
@Service
public class SESTransporter implements IEmailSender {
    private ConfigHandler _configHandler;
    private final Logger LOGGER = LogManager.getLogger(SESTransporter.class);
    private AWSCredentials _credentials;

    @Autowired
    public SESTransporter(ConfigHandler configHandler) {
        _configHandler = configHandler;
    }

    /**
     * Gửi Email
     */
    @Override
    public boolean sendEmail(String from, String to, String[] cc, String[] bcc, String subject, String htmlContent, String attachment) {
        try {
            if (attachment != null && !attachment.isEmpty()) {
                throw new NotImplementedException();
            }

            AWSCredentialsProvider credentialsProvider = new PolibeeAWSCredentialsProvider();
            String region = "ap-southeast-1";
            try {
                region = _configHandler.getConfig("email.transporter.region", "ap-southeast-1");
            } catch (InvalidConfigException e) {
                e.printStackTrace();
            }

            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withCredentials(credentialsProvider)
                            .withRegion(region)
                            .build();

            Destination destination = new Destination()
                    .withToAddresses(to)
                    .withCcAddresses(cc)
                    .withBccAddresses(bcc);

            Content mailSubject = new Content().withData(subject);
            Content htmlBody = new Content().withData(htmlContent);
            Body body = new Body().withHtml(htmlBody);

            // Create a message with the specified subject and body.
            Message message = new Message().withSubject(mailSubject).withBody(body);

            SendEmailRequest sendEmailRequest = new SendEmailRequest()
                    .withSource(from)
                    .withDestination(destination)
                    .withMessage(message);

            client.sendEmail(sendEmailRequest);
        } catch (Exception ex) {
            LOGGER.error("SES Message send failed", ex);
            return false;
        }
        return true;
    }

    /**
     * Tạo credential theo config handler được đăng ký
     */
    class PolibeeAWSCredentialsProvider implements AWSCredentialsProvider {
        @Override
        public AWSCredentials getCredentials() {
            if (_credentials == null) {
                refresh();
            }

            return _credentials;
        }

        @Override
        public void refresh() {
            _credentials = new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    try {
                        return _configHandler.getConfig("email.transporter.accessKeyId");
                    } catch (InvalidConfigException e) {
                        LOGGER.error("Could not read config for AWS SES client", e);
                    }
                    return "";
                }

                @Override
                public String getAWSSecretKey() {
                    try {
                        return _configHandler.getConfig("email.transporter.secretKey");
                    } catch (InvalidConfigException e) {
                        LOGGER.error("Could not read config for AWS SES client", e);
                    }
                    return "";
                }
            };
        }
    }
}