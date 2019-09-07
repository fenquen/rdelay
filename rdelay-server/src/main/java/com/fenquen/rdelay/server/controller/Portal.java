package com.fenquen.rdelay.server.controller;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.Task;
import com.fenquen.rdelay.model.req.Req4Create;
import com.fenquen.rdelay.model.resp.Resp4Create;
import com.fenquen.rdelay.model.resp.Resp4Query;
import com.fenquen.rdelay.model.resp.RespBase;
import com.fenquen.rdelay.server.redis.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

@RestController
public class Portal {

    @Autowired
    private RedisOperator redisOperator;

    @RequestMapping("/create")
    public RespBase create(@RequestParam(name = "req4Create") String req4CreateStr) {
        Resp4Create resp4Create = new Resp4Create();

        try {
            Req4Create req4Create = JSON.parseObject(req4CreateStr, Req4Create.class);//parseHttpReq(httpReq, Req4Create.class);
            Task task = Task.buildTaskByReq4Create(req4Create);
            redisOperator.createTask(task);

            resp4Create.id = task.id;
            resp4Create.success();
        } catch (Exception e) {
            resp4Create.fail(e);
        }

        return resp4Create;
    }

    @RequestMapping("/query")
    public RespBase query(@RequestParam String id) {
        Resp4Query resp4Query = new Resp4Query();
        try {
            String taskJsonStr = redisOperator.getTaskJsonStr(id);
            if (!StringUtils.hasText(taskJsonStr)) {
                resp4Query.task = JSON.parseObject(taskJsonStr, Task.class);
            }
            resp4Query.success();
        } catch (Exception e) {
            resp4Query.fail(e);
        }

        return resp4Query;
    }

    @RequestMapping("/delete")
    public RespBase delete(String id) {
        RespBase respBase = new RespBase();
        try {
            redisOperator.deleteTask(id);
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
}
