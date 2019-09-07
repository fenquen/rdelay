package com.fenquen.rdelay.client.send;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.Task;
import com.fenquen.rdelay.model.req.Req4CreateTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;
import com.fenquen.rdelay.utils.HttpUtils;

public class TaskSender {
    private static String destURL = "http://127.0.0.1:8086/createTask";

    public static Resp4CreateTask sendTask(Req4CreateTask req4CreateTask) throws Exception {
        return JSON.parseObject(HttpUtils.postStringContent(destURL, JSON.toJSONString(req4CreateTask)), Resp4CreateTask.class);
    }

    public static void setDestURL(String destURL) {
        TaskSender.destURL = destURL;
    }
}
