package com.fenquen.rdelay.server.controller;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.Task;
import com.fenquen.rdelay.model.req.Req4CreateTask;
import com.fenquen.rdelay.model.req.Req4DelTask;
import com.fenquen.rdelay.model.req.Req4QueryTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;
import com.fenquen.rdelay.model.resp.Resp4Query;
import com.fenquen.rdelay.model.resp.RespBase;
import com.fenquen.rdelay.server.redis.RedisOperator;
import com.fenquen.rdelay.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;

@RestController
public class Portal {

    @Autowired
    private RedisOperator redisOperator;

    @RequestMapping(value = "/createTask", method = RequestMethod.POST)
    public RespBase create(@RequestBody Req4CreateTask req4Create) {
        Resp4CreateTask resp4CreateTask = new Resp4CreateTask();
        try {
            Task task = buildTaskByReq4Create(req4Create);
            redisOperator.createTask(task);

            resp4CreateTask.id = task.id;
            resp4CreateTask.success();
        } catch (Exception e) {
            resp4CreateTask.fail(e);
        }

        return resp4CreateTask;
    }

    @RequestMapping("/queryTask")
    public RespBase query(@RequestBody Req4QueryTask req4QueryTask) {
        Resp4Query resp4Query = new Resp4Query();
        try {
            String taskJsonStr = redisOperator.getTaskJsonStr(req4QueryTask.taskId);
            if (StringUtils.hasText(taskJsonStr)) {
                resp4Query.task = JSON.parseObject(taskJsonStr, Task.class);
            }
            resp4Query.success();
        } catch (Exception e) {
            resp4Query.fail(e);
        }

        return resp4Query;
    }

    @RequestMapping("/deleteTask")
    public RespBase delete(@RequestBody Req4DelTask req4DelTask) {
        RespBase respBase = new RespBase();
        try {
            redisOperator.deleteTask(req4DelTask.taskId);
            respBase.success();
        } catch (Exception e) {
            respBase.fail(e);
        }
        return respBase;
    }


    private <T> T parseHttpReq(HttpServletRequest httpReq, Class<T> type) throws Exception {
        ServletInputStream servletInputStream = httpReq.getInputStream();
        byte[] reqBody = new byte[servletInputStream.available()];
        servletInputStream.read(reqBody);

        String jsonStr = new String(reqBody);

        return JSON.parseObject(jsonStr, type);
    }

    private Task buildTaskByReq4Create(Req4CreateTask req4Create) {

        Task task = new Task();

        task.id = UUID.randomUUID().toString();
        task.bizTag = req4Create.bizTag;
        task.executionTime = req4Create.executionTime;
        task.maxRetryCount = req4Create.maxRetryCount;
        task.executionAddr = TextUtils.verifyAndModifyHttpSvrAddr(req4Create.executionAddr);
        task.content = req4Create.content;
        task.createTime = new Date().getTime();

        return task;
    }
}
