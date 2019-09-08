package com.fenquen.rdelay.server.controller;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.req.create_task.Req4CreateReflectTask;
import com.fenquen.rdelay.model.req.create_task.Req4CreateStrContentTask;
import com.fenquen.rdelay.model.task.AbstractTask;
import com.fenquen.rdelay.model.req.create_task.Req4CreateTask;
import com.fenquen.rdelay.model.req.Req4DelTask;
import com.fenquen.rdelay.model.req.Req4QueryTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;
import com.fenquen.rdelay.model.resp.Resp4Query;
import com.fenquen.rdelay.model.resp.RespBase;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
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
            AbstractTask task = parseReq4Create(req4Create);
            redisOperator.createTask(task);

            resp4CreateTask.id = task.id;
            resp4CreateTask.success();
        } catch (Exception e) {
            resp4CreateTask.fail(e);
        }

        return resp4CreateTask;
    }

    // @RequestMapping("/queryTask")
    public RespBase query(@RequestBody Req4QueryTask req4QueryTask) {
        Resp4Query resp4Query = new Resp4Query();
        try {
            String taskJsonStr = redisOperator.getTaskJsonStr(req4QueryTask.taskId);
            if (StringUtils.hasText(taskJsonStr)) {
                resp4Query.task = JSON.parseObject(taskJsonStr, AbstractTask.class);
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

    @SuppressWarnings("ConstantConditions")
    private AbstractTask parseReq4Create(Req4CreateTask req4Create) {
        AbstractTask abstractTask;

        switch (req4Create.taskType) {
            case STR_CONTENT:
                abstractTask = new StrContentTask();
                break;
            case REFLECT:
                abstractTask = new ReflectionTask();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        // common part
        abstractTask.id = UUID.randomUUID().toString();
        abstractTask.bizTag = req4Create.bizTag;
        abstractTask.executionTime = req4Create.executionTime;
        abstractTask.maxRetryCount = req4Create.maxRetryCount;
        abstractTask.executionAddr = req4Create.executionAddr;
        abstractTask.createTime = new Date().getTime();
        abstractTask.taskType = req4Create.taskType;

        // custom part
        switch (req4Create.taskType) {
            case STR_CONTENT:
                ((StrContentTask) abstractTask).content = ((Req4CreateStrContentTask) req4Create).content;
                break;
            case REFLECT:
                ReflectionTask reflectionTask = (ReflectionTask) abstractTask;
                Req4CreateReflectTask req4CreateReflectTask = (Req4CreateReflectTask) req4Create;

                reflectionTask.className = req4CreateReflectTask.className;
                reflectionTask.methodName = req4CreateReflectTask.methodName;
                reflectionTask.paramTypeNames = req4CreateReflectTask.paramTypeNames;
                reflectionTask.params = req4CreateReflectTask.params;
                break;
        }


        return abstractTask;
    }
}
