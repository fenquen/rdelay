package com.fenquen.rdelay.model;

public abstract class ModelBase {
    public abstract ModelType getModel();

    public enum ModelType {
        TASK, EXECUTION_RESP
    }
}
