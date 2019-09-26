package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.Persistence;
import com.fenquen.rdelay.model.task.TaskBase;

public class ExecutionResp extends RespBase implements Persistence {
    public String taskid;

    public String taskName;

    public long executionTime;

    public ExecutionResp(TaskBase taskBase) {
        taskid = taskBase.taskid;
        taskName = taskBase.name;
    }

    public ExecutionResp() {

    }

    @Override
    void failInternal(Throwable throwable) {
    }

    @Override
    void successInternal() {
        executionTime = System.currentTimeMillis();
    }

    @Override
    public DbMetaData getDbMetaData() {
        return DbMetaData.EXECUTION_RESP;
    }
}
