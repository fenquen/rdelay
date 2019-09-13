package com.fenquen.rdelay.model.task;

public final class StrContentTask extends TaskBase {

    /**
     * usually a json string
     */
    public String content;

    @Override
    public DbMetaData getDbMetaData() {
        return DbMetaData.TASK;
    }
}
