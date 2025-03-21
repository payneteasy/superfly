package com.payneteasy.superfly.web.conroller.api;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class ExceptionResponseModel {
    private final String errorId;
    private final String messageId;
    private final String details;

    private ExceptionResponseModel(String errorId, String messageId, String details) {
        Objects.requireNonNull(errorId);
        Objects.requireNonNull(messageId);

        this.errorId = errorId;
        this.messageId = messageId;
        this.details = details;
    }

    public static ExceptionResponseModel createErrorModel(String aMessageId, String aDetails) {
        return new ExceptionResponseModel(
                UUID.randomUUID().toString()
                , aMessageId
                , aDetails
        );
    }

}
