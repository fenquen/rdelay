package com.fenquen.rdealy.example.cli;

import com.fenquen.rdealy.client.sender.RdelayCli;
import com.fenquen.rdelay.model.req.modify_task.Req4AbortTaskManually;
import com.fenquen.rdelay.model.req.modify_task.Req4PauseTask;
import com.fenquen.rdelay.model.req.modify_task.Req4ResumeTask;
import com.fenquen.rdelay.model.resp.RespBase;

public class TaskLifecycle {
    public static void main(String[] args) throws Exception {
        abortTaskManually("STR_CONTENT@953514ad-d140-46c7-a3e0-85fc56a27151");
    }

    public static void pauseTask(String taskid) throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4PauseTask(taskid));
        System.out.println(resp.success + "_" + resp.errMsg);
    }

    public static void resumeTask(String taskid) throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4ResumeTask(taskid));
        System.out.println(resp.success + "_" + resp.errMsg);
    }

    // the task is aborted, it can not be available any more
    public static void abortTaskManually(String taskid) throws Exception {
        RespBase resp = RdelayCli.modifyTaskState(new Req4AbortTaskManually(taskid));
        System.out.println(resp.success + "_" + resp.errMsg);
    }
}
