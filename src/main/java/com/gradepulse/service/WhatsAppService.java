package com.gradepulse.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class WhatsAppService {

    @Value("${twilio.account.sid}") private String sid;
    @Value("${twilio.auth.token}") private String token;
    @Value("${twilio.whatsapp.from}") private String from;

    public void send(String to, String body) {
        if (to == null || to.trim().isEmpty()) return;
        Twilio.init(sid, token);
        Message.creator(
            new PhoneNumber("whatsapp:" + to),
            new PhoneNumber(from),
            body
        ).create();
    }
}