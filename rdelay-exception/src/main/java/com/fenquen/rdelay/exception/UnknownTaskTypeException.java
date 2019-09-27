package com.fenquen.rdelay.exception;

public class UnknownTaskTypeException extends Exception {
    public UnknownTaskTypeException() {
        super();
    }

    public UnknownTaskTypeException(String msg) {
        super(msg);
    }


    public UnknownTaskTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
