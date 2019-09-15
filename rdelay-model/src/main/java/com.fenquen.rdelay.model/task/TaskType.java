package com.fenquen.rdelay.model.task;

public enum TaskType {
    STR_CONTENT(StrContentTask.class), REFLECTION(ReflectionTask.class);

    public Class<? extends TaskBase> clazz;

    TaskType(Class<? extends TaskBase> clazz) {
        this.clazz = clazz;
    }
}
