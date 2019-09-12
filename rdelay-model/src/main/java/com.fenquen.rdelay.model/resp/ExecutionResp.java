package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.resp.RespBase;
import com.fenquen.rdelay.model.task.AbstractTask;

public class ExecutionResp extends RespBase {
    public String taskId;

    public ExecutionResp(AbstractTask abstractTask) {
        taskId = abstractTask.id;
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
    public ModelType getModel() {
        return ModelType.EXECUTION_RESP;
    }
}
