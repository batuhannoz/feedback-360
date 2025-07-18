package com.batuhan.feedback360.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final MessageSource messageSource;

    public String getMessage(String code, Object... args) {
        String message = null;
        try {
            message = messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            message = code;
        }
        return message;
    }
}
