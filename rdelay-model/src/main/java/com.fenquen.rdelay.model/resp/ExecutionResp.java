package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.Persistence;
import com.fenquen.rdelay.model.task.TaskBase;

public class ExecutionResp extends RespBase implements Persistence {
    public String taskid;

    public String taskName;

    public Long expectedExecutionTime;

    public Long executionTime;

    public Long executionEndTime;

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
        executionTime = System.currentTimeMillis();
    }

    public ExecutionResp(TaskBase taskBase, Throwable e) {
        this(taskBase);
        executionTime = null;
        fail(e);
    }

    public ExecutionResp() {

    }

    @Override
    void failInternal(Throwable throwable) {
    }

    @Override
    void successInternal() {
        executionEndTime = System.currentTimeMillis();
    }

    @Override
    public DbMetaData getDbMetaData() {
        return DbMetaData.EXECUTION_RESP;
    }
}
