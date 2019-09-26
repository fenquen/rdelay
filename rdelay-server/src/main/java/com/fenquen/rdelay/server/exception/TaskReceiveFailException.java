package com.fenquen.rdelay.server.exception;

public class TaskReceiveFailException extends Exception {
    public TaskReceiveFailException(){

    }

    public TaskReceiveFailException(String msg){
        super(msg);
    }
}
