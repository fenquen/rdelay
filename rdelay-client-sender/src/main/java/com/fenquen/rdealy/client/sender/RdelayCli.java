package com.fenquen.rdealy.client.sender;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.req.ReqBase;
import com.fenquen.rdelay.model.req.modify_task.Req4AbortTaskManually;
import com.fenquen.rdelay.model.req.modify_task.Req4ModifyTask;
import com.fenquen.rdelay.model.req.modify_task.Req4PauseTask;
import com.fenquen.rdelay.model.req.modify_task.Req4ResumeTask;
import com.fenquen.rdelay.model.resp.RespBase;
import com.fenquen.rdelay.model.task.TaskType;
import com.fenquen.rdelay.model.req.create_task.Req4CreateTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;
import com.fenquen.rdelay.utils.HttpUtils;
import com.fenquen.rdelay.utils.TextUtils;

public class RdelayCli {
    private static String DEST_URL_PRE = "http://127.0.0.1:8086";

    public static Resp4CreateTask sendTask(Req4CreateTask req4CreateTask) throws Exception {
        req4CreateTask.verifyFields();

        String destUrl = "";
        switch (req4CreateTask.getTaskType()) {
            case REFLECT:
                destUrl += DEST_URL_PRE + req4CreateTask.getRequestUri();
                break;
            case STR_CONTENT:
                destUrl += DEST_URL_PRE + req4CreateTask.getRequestUri();
        }

        return JSON.parseObject(HttpUtils.postStringContentSync(destUrl, JSON.toJSONString(req4CreateTask)), Resp4CreateTask.class);
    }

    /**
     * pause,resume or abortManually
     *
     * @param req4ModifyTask
     * @return
     * @throws Exception
     */
    public static <T extends Req4ModifyTask> RespBase modifyTask(T req4ModifyTask) throws Exception {
        req4ModifyTask.verifyFields();
        return JSON.parseObject(HttpUtils.postStringContentSync(DEST_URL_PRE + req4ModifyTask.getRequestUri(), JSON.toJSONString(req4ModifyTask)), RespBase.class);
    }


    /**
     * designate the rdelay server address where the task to be sent
     *
     * @param destSvrAddr rdelay server address
     */
    public static void setDestSvrAddr(String destSvrAddr) {
        RdelayCli.DEST_URL_PRE = TextUtils.verifyAndModifyHttpSvrAddr(destSvrAddr);
    }

}
