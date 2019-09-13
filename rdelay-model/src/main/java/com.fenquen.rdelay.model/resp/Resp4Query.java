package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.task.AbstractTask;

public class Resp4Query extends RespBase {
    public AbstractTask task;

    @Override
    public DbMetaData getDbMetaData() {
        return null;
    }
}
