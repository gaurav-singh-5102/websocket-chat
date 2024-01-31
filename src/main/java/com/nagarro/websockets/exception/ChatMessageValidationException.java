package com.nagarro.websockets.exception;

public class ChatMessageValidationException extends Exception {

    public ChatMessageValidationException() {
        super("Null Fields found in chat message");
    }

}
