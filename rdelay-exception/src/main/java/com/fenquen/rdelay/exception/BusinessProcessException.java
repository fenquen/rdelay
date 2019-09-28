package com.fenquen.rdelay.exception;

public class BusinessProcessException extends Exception {
    public BusinessProcessException() {

    }

    public BusinessProcessException(String msg) {
        super(msg);
    }


    public BusinessProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessProcessException(Throwable cause) {
        super(cause);
    }
}
