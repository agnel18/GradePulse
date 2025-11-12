package com.gradepulse.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;
import io.github.cdimascio.dotenv.Dotenv;

@Service
public class WhatsAppService {

    private final String accountSid;
    private final String authToken;
    private final String whatsappNumber;

    public WhatsAppService() {
        Dotenv dotenv = Dotenv.configure()
                             .directory("./")  // Look in project root
                             .ignoreIfMissing()
                             .load();

        this.accountSid = dotenv.get("TWILIO_SID");
        this.authToken = dotenv.get("TWILIO_TOKEN");
        this.whatsappNumber = dotenv.get("TWILIO_WHATSAPP_NUMBER");

        if (accountSid == null || authToken == null || whatsappNumber == null) {
            throw new IllegalStateException("Missing Twilio config in .env file!");
        }

        Twilio.init(accountSid, authToken);
    }

    public void send(String to, String message) {
        Message.creator(
            new PhoneNumber("whatsapp:" + to),
            new PhoneNumber(whatsappNumber),
            message
        ).create();
    }
}