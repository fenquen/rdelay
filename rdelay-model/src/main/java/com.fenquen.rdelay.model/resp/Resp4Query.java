package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.task.TaskBase;

public class Resp4Query extends RespBase {
    public TaskBase task;

    @Override
    public DbMetaData getDbMetaData() {
        return null;
    }
}
