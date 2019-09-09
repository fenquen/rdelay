package com.fenquen.rdealy.client.sender;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.TaskType;
import com.fenquen.rdelay.model.req.create_task.Req4CreateTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;
import com.fenquen.rdelay.utils.HttpUtils;
import com.fenquen.rdelay.utils.TextUtils;

public class TaskSender {
    private static String DEST_URL_PRE = "http://127.0.0.1:8086/createTask/";

    public static Resp4CreateTask sendTask(Req4CreateTask req4CreateTask) throws Exception {
        req4CreateTask.verifyFields();

        String destUrl = "";
        switch (req4CreateTask.getTaskType()) {
            case REFLECT:
                destUrl += DEST_URL_PRE + TaskType.REFLECT.name();
                break;
            case STR_CONTENT:
                destUrl += DEST_URL_PRE + TaskType.STR_CONTENT.name();
        }

        return JSON.parseObject(HttpUtils.postStringContent(destUrl, JSON.toJSONString(req4CreateTask)), Resp4CreateTask.class);
    }

    /**
     * designate the rdelay server address where the task to be sent
     *
     * @param destSvrAddr rdelay server address
     */
    public static void setDestSvrAddr(String destSvrAddr) {
        TaskSender.DEST_URL_PRE = TextUtils.verifyAndModifyHttpSvrAddr(destSvrAddr) + "/createTask/";
    }

}
