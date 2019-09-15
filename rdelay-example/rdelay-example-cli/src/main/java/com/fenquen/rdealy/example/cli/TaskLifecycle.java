package com.fenquen.rdealy.example.cli;

import com.fenquen.rdealy.client.sender.RdelayCli;
import com.fenquen.rdelay.model.req.modify_task.Req4AbortTaskManually;
import com.fenquen.rdelay.model.req.modify_task.Req4PauseTask;
import com.fenquen.rdelay.model.req.modify_task.Req4ResumeTask;
import com.fenquen.rdelay.model.resp.RespBase;

public class TaskLifecycle {
    public static void pauseTask() throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4PauseTask("your taskid"));
        System.out.println(resp.success + "_" + resp.errMsg);
    }

    public static void resumeTask() throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4ResumeTask("your taskid"));
        System.out.println(resp.success + "_" + resp.errMsg);
    }

    // the task is aborted, it can not be available any more
    public static void abortTaskManually() throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4AbortTaskManually("your taskid"));
        System.out.println(resp.success + "_" + resp.errMsg);
    }
}
