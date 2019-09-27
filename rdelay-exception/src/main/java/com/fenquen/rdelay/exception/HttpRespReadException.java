package com.fenquen.rdelay.exception;

public class HttpRespReadException extends Exception {
    public HttpRespReadException(){

    }

    public HttpRespReadException(String msg) {
        super(msg);
    }


    public HttpRespReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRespReadException(Throwable cause) {
        super(cause);
    }
}
