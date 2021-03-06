package com.fenquen.rdelay.model.task;

/**
 * use reflection to invoke the method on target application
 */
public final class ReflectionTask extends TaskBase {

    public String className;

    public String methodName;

    public String[] paramTypeNames;

    public String[] params;

    @Override
    public DbMetaData getDbMetaData() {
        return DbMetaData.TASK;
    }
}
