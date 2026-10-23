package com.payguard.notification.service;

import com.payguard.notification.api.Smsc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMSService {

    private final Smsc smsc;

    public String[] sendSMS(String phone, String message) {
        // Removing + from the start of the number
        String[] strings = smsc.sendSMS(phone.substring(1), message, 1, "", "", 0, "", "");
        for (String s : strings) {
            log.info("{}", s);
        }
        return strings;
    }
}
