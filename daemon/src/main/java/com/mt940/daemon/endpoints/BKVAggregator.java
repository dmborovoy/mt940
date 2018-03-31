package com.mt940.daemon.endpoints;

import com.mt940.daemon.BKVHeaders;
import com.mt940.domain.EARAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.ReleaseStrategy;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service("bkvAggregator")
public class BKVAggregator {

    private static final Logger log = LoggerFactory.getLogger(BKVAggregator.class);

    @Value("${bkv.daemon.email.sender.max-message-to-send}")
    private int maxMessageToSend;

    @Value("${bkv.daemon.email.sender.subject.ok}")
    private String subjectOk;

    @Value("${bkv.daemon.email.sender.subject.error}")
    private String subjectError;

    @Aggregator
    public Message<?> aggregate(final List<Message<List<EARAttachment>>> in) throws MessagingException {
        log.info("$$$$$ aggregating....");
        log.info("Message count: {}", in.size());
        if (in.size() > 0) {
            return MessageBuilder.withPayload(okBody(in))
                    .setHeader(MailHeaders.SUBJECT, subjectOk)
                    .build();
        } else { //TODO DB: unreachable statement. Review channels
            return MessageBuilder.withPayload(errorBody(in))
                    .setHeader(MailHeaders.SUBJECT, subjectError)
                    .build();
        }
    }

    private String errorBody(List<Message<List<EARAttachment>>> in) throws MessagingException {
        return "Warning. At predefined time target email box doesn't contain new messages for processing.\nPing admins to check consistency of bkv-daemon module configuration";
    }

    private String okBody(List<Message<List<EARAttachment>>> in) throws MessagingException {
        StringBuilder builder = new StringBuilder("===============================================\n");
        builder.append(String.format("Was processed '%d' new email(s):\n", in.size()));
        builder.append("===============================================\n\n");
        for (Message<List<EARAttachment>> message : in) {
            String subj = "Subject: " + ((MimeMessage) message.getHeaders().get(BKVHeaders.MAIL_MESSAGE)).getSubject();
            String sent = String.format("Sent date: %s", ((MimeMessage) message.getHeaders().get(BKVHeaders.MAIL_MESSAGE)).getSentDate());
            log.debug(subj);
            log.debug(sent);
            builder.append(String.format("%s\n%s\nFile list (%d) :\n", subj, sent, message.getPayload().size()));
            for (EARAttachment el : message.getPayload()) {
                String out = String.format("\t%-70s-\t[%s];\n", el.getUniqueName(), el.getStatus());
                log.debug(out);
                builder.append(out);
            }
            builder.append("---------------------------------\n");
        }
        return builder.append("\nFor details visit bkv-ui site").toString();
    }


    @CorrelationStrategy
    public String correlateBy(@Header("id") String id, @Header(IntegrationMessageHeaderAccessor.CORRELATION_ID) String correlationId) {
        log.debug("id: {}; correlation id: {}", id, correlationId);
        return "BKVGroup";
    }

    @ReleaseStrategy
    public boolean release(List<Message<?>> messages) {
        return messages.size() == maxMessageToSend;
    }
}