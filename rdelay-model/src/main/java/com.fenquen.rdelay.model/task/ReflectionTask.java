package com.fenquen.rdelay.model.task;

/**
 * use reflection to invoke the method on target application
 */
public class ReflectionTask extends AbstractTask {

    public String className;

    public String methodName;

    public String[] paramTypeNames;

    public String[] params;

}
