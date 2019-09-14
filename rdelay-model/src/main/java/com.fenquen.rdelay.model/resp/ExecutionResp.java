package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.Persistence;
import com.fenquen.rdelay.model.task.TaskBase;

public class ExecutionResp extends RespBase implements Persistence {
    public String taskId;

    public ExecutionResp(TaskBase abstractTask) {
        taskId = abstractTask.taskid;
    }

    public ExecutionResp() {

    }

    @Override
    public void failInternal(Throwable throwable) {
        super.failInternal(throwable);
    }

    @Override
    protected void successInternal() {
        super.successInternal();
    }

    @Override
    public DbMetaData getDbMetaData() {
        return DbMetaData.EXECUTION_RESP;
    }
}
