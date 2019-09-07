package com.fenquen.rdelay.client.send;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.req.Req4CreateTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;
import com.fenquen.rdelay.utils.HttpUtils;

import java.util.regex.Pattern;

public class TaskSender {
    private static String RDELAY_HTTP_SVR_ADDR = "http://127.0.0.1:8086";

    public static Resp4CreateTask sendTask(Req4CreateTask req4CreateTask) throws Exception {
        return JSON.parseObject(HttpUtils.postStringContent(RDELAY_HTTP_SVR_ADDR + "/createTask", JSON.toJSONString(req4CreateTask)), Resp4CreateTask.class);
    }

    public static void setRdelayHttpSvrAddr(String rdelayHttpSvrAddr) {
        TaskSender.RDELAY_HTTP_SVR_ADDR = rdelayHttpSvrAddr;
    }
}
