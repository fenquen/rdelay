package com.fenquen.rdealy.client.send;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.req.create_task.Req4CreateTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;
import com.fenquen.rdelay.utils.HttpUtils;
import com.fenquen.rdelay.utils.TextUtils;

public class TaskSender {
    private static final String DEST_URI = "/createTask";

    private static String DEST_URL = "http://127.0.0.1:8086/createTask";

    public static Resp4CreateTask sendTask(Req4CreateTask req4CreateTask) throws Exception {
        req4CreateTask.verifyFields();
        return JSON.parseObject(HttpUtils.postStringContent(DEST_URL, JSON.toJSONString(req4CreateTask)), Resp4CreateTask.class);
    }

    public static void setDestUrl(String destSvrAddr) {
        TaskSender.DEST_URL = TextUtils.verifyAndModifyHttpSvrAddr(destSvrAddr) + DEST_URI;
    }

}
