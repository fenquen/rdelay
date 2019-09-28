package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.Persistence;
import com.fenquen.rdelay.model.task.TaskBase;

public class ExecutionResp extends RespBase implements Persistence {
    public String taskid;

    public String taskName;

    public long expectedExecutionTime;

    public long executionTime;

    public Boolean retry;

    /**
     * null means server did not get the receiveResp, not know whether receive is successful or not
     */
    public ReceiveResp receiveResp;

    public ExecutionResp(TaskBase taskBase) {
        taskid = taskBase.taskid;
        taskName = taskBase.name;
        expectedExecutionTime = taskBase.executionTime;
        retry = taskBase.retriedCount > 0;
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
