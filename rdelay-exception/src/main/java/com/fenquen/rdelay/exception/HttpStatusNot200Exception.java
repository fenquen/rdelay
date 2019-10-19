package com.fenquen.rdelay.exception;

public class HttpStatusNot200Exception extends Exception {
    public HttpStatusNot200Exception() {

    }

    public HttpStatusNot200Exception(String msg) {
        super(msg);
    }


    public HttpStatusNot200Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpStatusNot200Exception(Throwable cause) {
        super(cause);
    }
}
