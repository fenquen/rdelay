package com.fenquen.rdelay.model.task;

public enum TaskType {
    STR_CONTENT(StrContentTask.class), REFLECT(ReflectionTask.class);

    public Class<? extends TaskBase> clazz;

    TaskType(Class<? extends TaskBase> clazz) {
        this.clazz = clazz;
    }
}
